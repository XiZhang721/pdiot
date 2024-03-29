package com.specknet.pdiotapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.utils.Constants
import kotlinx.android.synthetic.main.activity_connect_main.*
import java.util.*

class MainConnectActivity: AppCompatActivity(){
    private var layoutManager: RecyclerView.LayoutManager ?= null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder> ?= null

    lateinit var refreshButton: ImageButton
    lateinit var addButton: ImageButton
    private var hintShowing: Boolean = false
    lateinit var hintText: TextView
    private var timer: Timer = Timer()

    private var thingyOn = false
    private var respeckOn = false

    lateinit var respeckReceiver: BroadcastReceiver
    lateinit var thingyReceiver: BroadcastReceiver
    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_main)

        // Sets the cards of Respeck and Thingy
        layoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = layoutManager
        adapter = RecyclerAdapter(this,respeckOn ,thingyOn)
        recycleView.adapter = adapter
        var mainContext: Context = this
        
        // Update the connection state of respeck
        respeckReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST && !respeckOn) {
                    respeckOn = true
                    runOnUiThread {
                        recycleView.adapter = RecyclerAdapter(mainContext,respeckOn ,thingyOn)
                    }
                }
            }
        }

        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        val looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckReceiver, filterTestRespeck, null, handlerRespeck)

        // Update the connection state of thingy
        thingyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Constants.ACTION_THINGY_BROADCAST && !thingyOn) {
                    thingyOn = true
                    runOnUiThread {
                        recycleView.adapter = RecyclerAdapter(mainContext,respeckOn ,thingyOn)
                    }
                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        var looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        this.registerReceiver(thingyReceiver, filterTestThingy, null, handlerThingy)

        // Sets the bottom navigator
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
                R.id.user->{
                    val intent = Intent(this, UserActivity::class.java)
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

    /**
        This function set up click listeners for the refresh button and add button.
     */
    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            thingyOn = false
            respeckOn = false
            recycleView.adapter =RecyclerAdapter(this,respeckOn ,thingyOn)
        }

        addButton.setOnClickListener {
            hintTimeCount()
        }
    }

    /**
     * This function shows the hint text.
     */
    private fun showHint() {
        if(hintShowing){
            return
        }
        hintText.text = getString(R.string.add_hint)
        hintShowing = true
    }

    /**
     * This function removes the hint text when timeout is reached.
     */
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

    override fun onDestroy() {
        unregisterReceiver(respeckReceiver)
        unregisterReceiver(thingyReceiver)
        super.onDestroy()
    }
}