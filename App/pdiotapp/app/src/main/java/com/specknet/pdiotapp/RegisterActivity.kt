package com.specknet.pdiotapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.specknet.pdiotapp.MainConnectActivity
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.utils.Utils
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection
import java.util.*

class RegisterActivity: AppCompatActivity(){
    lateinit var usernameInput: EditText
    lateinit var password1: EditText
    lateinit var password2: EditText
    lateinit var registerButton: Button
    lateinit var loginButton: Button
    lateinit var hintText: TextView
    private var hintShowing: Boolean = false
    private var timer: Timer = Timer()
    private var registerUrl: String =  "https://pdiot-c.ew.r.appspot.com/register"
    private lateinit var ccon: CloudConnection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameInput = findViewById(R.id.username_register_input)
        password1 = findViewById(R.id.password1_register_input)
        password2 = findViewById(R.id.password2_register_input)
        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_page_loader)
        hintText = findViewById(R.id.register_hint)
        hintText.text = ""
        password1.transformationMethod = PasswordTransformationMethod.getInstance()
        password2.transformationMethod = PasswordTransformationMethod.getInstance()

        setupClickListeners()


    }
    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            var isPasswordMatching: Boolean = password1.text.toString() == password2.text.toString()
            if (!isPasswordMatching) {
                hintTimeCount(1)
            } else {
                var isRegisterSuccess: Boolean = registerCheck(usernameInput.text.toString(), password1.text.toString())
                if (isRegisterSuccess) {
                    showHint(0)
                } else {
                    hintTimeCount(2)
                }
            }
            usernameInput.text.clear()
            password1.text.clear()
            password2.text.clear()
        }
        loginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
    private fun showHint(hint_type: Int) {
        when (hint_type){
            0-> {
                hintText.text = getString(R.string.register_success)
                hintText.setTextColor(Color.parseColor("#00e800"))
            }
            1->{
                hintText.text = getString(R.string.register_password)
                hintText.setTextColor(Color.parseColor("#FF0000"))
            }
            2->{
                hintText.text = getString(R.string.register_username)
                hintText.setTextColor(Color.parseColor("#FF0000"))
            }
            else->{
            throw IllegalArgumentException("Invalid hint type for registration.")
            }
        }

        hintShowing = true
    }

    private fun hintTimeCount(hint_type: Int){
        var hideTask: TimerTask = object : TimerTask(){
            override fun run() {
                if(!hintShowing){
                    return
                }
                runOnUiThread(){
                    hintText.text = ""
                }

                hintShowing = false
            }
        }
        if(hintShowing){
            timer.cancel()
            timer = Timer()
        }
        showHint(hint_type)
        timer.schedule(hideTask,3000)

    }

    private fun registerCheck(username: String, password: String):Boolean{
        print(username)
        print(password)
        if(username.isEmpty() || password.isEmpty()){
            return false
        }
        try {

            ccon =  CloudConnection.setUpUserDataConnection(registerUrl)
            var response:String = "";
            var thr = Thread(Runnable{
                response = ccon.sendRegisterPostRequest(username, password)

            })
            thr.start()
            while (response == ""){}
            thr.interrupt()
            ccon.disconnect()

            if (response == "1") {
                return true
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
        return false


    }
}