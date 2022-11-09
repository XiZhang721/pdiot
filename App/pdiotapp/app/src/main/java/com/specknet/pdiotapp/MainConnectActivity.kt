package com.specknet.pdiotapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.specknet.pdiotapp.live.LiveDataActivity
import kotlinx.android.synthetic.main.activity_connect_main.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainConnectActivity: AppCompatActivity(){
    private var layoutManager: RecyclerView.LayoutManager ?= null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder> ?= null

    lateinit var refreshButton: ImageButton
    lateinit var addButton: ImageButton
    private var hintShowing: Boolean = false
    lateinit var hintText: TextView
    private var timer: Timer = Timer()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_main)
        layoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = layoutManager
        adapter = RecyclerAdapter(this)
        recycleView.adapter = adapter
        var bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.connection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.connection ->{
                    true
                }
                R.id.live->{
                    val intent = Intent(this, LiveDataActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    true
                }
                R.id.record->{
                    val intent = Intent(this, RecordingActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    true
                }
            }
            false
        }
        refreshButton = findViewById(R.id.refresh_button)
        addButton = findViewById(R.id.add_button)
        hintText = findViewById(R.id.hintText)
        setupClickListeners()
    }
    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            val intent = Intent(this, this.javaClass)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            hintTimeCount()
        }
    }

    private fun showHint() {
        if(hintShowing){
            return
        }
        hintText.text = getString(R.string.add_hint)
        hintShowing = true
    }

    private fun hintTimeCount(){
        var hideTask: TimerTask = object : TimerTask(){
            override fun run() {
                if(!hintShowing){
                    return
                }
                hintText.text = ""
                hintShowing = false
            }
        }
        if(hintShowing){
            timer.cancel()
            timer = Timer()
            timer.schedule(hideTask,3000)
        }else{
            showHint()
            timer.schedule(hideTask,3000)
        }
    }
}