package com.specknet.pdiotapp.live

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.specknet.pdiotapp.*
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService
import com.specknet.pdiotapp.utils.Utils


class LiveDataActivity : AppCompatActivity() {

    lateinit var respeckButton:Button
    lateinit var thingyButton:Button
    lateinit var bothButton: Button
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)

        // Set the three buttons
        respeckButton = findViewById(R.id.respeck_button)
        thingyButton = findViewById(R.id.thingy_button)
        bothButton = findViewById(R.id.both_button)

        respeckButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            }else{
                val intent = Intent(this, RespeckActivity::class.java)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()

        }

        thingyButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            }else {
                val intent = Intent(this, ThingyActivity::class.java)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        bothButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            }else {
                val intent = Intent(this, BothActivity::class.java)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        // The three buttons will be set to disabled, and then enable them when sensors finish reconnecting
        respeckButton.isEnabled = false
        thingyButton.isEnabled = false
        bothButton.isEnabled = false

        startSpeckService()

        // Set the bottom navigator
        var bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.live
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.connection -> {
                    val intent = Intent(this, MainConnectActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.live -> {
                    true
                }
                R.id.user -> {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
            }
            false
        }
    }

    /**
        This function is used for re-connecting sensors. Message will be displayed to the user
        for sensors' initialization.
     */
    private fun startSpeckService() {
        // TODO if it's not already running
        val isServiceRunning = Utils.isServiceRunning(BluetoothSpeckService::class.java, applicationContext)
        Log.i("service","isServiceRunning = " + isServiceRunning)

        if (isServiceRunning){
            Log.i("service", "Service already running, restart")
            this.stopService(Intent(this, BluetoothSpeckService::class.java))
            Toast.makeText(this, "Sensors start initialization", Toast.LENGTH_SHORT).show()
            this.startService(Intent(this, BluetoothSpeckService::class.java))
            while(!Utils.isServiceRunning(BluetoothSpeckService::class.java, applicationContext)){}
            Toast.makeText(this, "Sensors finished initialization", Toast.LENGTH_SHORT).show()
            respeckButton.isEnabled = true
            thingyButton.isEnabled = true
            bothButton.isEnabled = true
        }else{
            Toast.makeText(this, "Please connect to a sensor first", Toast.LENGTH_SHORT).show()
        }
    }
}
