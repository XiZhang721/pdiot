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

    // global graph variables
//    lateinit var dataSet_res_accel_x: LineDataSet
//    lateinit var dataSet_res_accel_y: LineDataSet
//    lateinit var dataSet_res_accel_z: LineDataSet
//
//    lateinit var dataSet_thingy_accel_x: LineDataSet
//    lateinit var dataSet_thingy_accel_y: LineDataSet
//    lateinit var dataSet_thingy_accel_z: LineDataSet
//
//    var time = 0f
//    lateinit var allRespeckData: LineData
//
//    lateinit var allThingyData: LineData
//    lateinit var respeckChart: LineChart
//    lateinit var thingyChart: LineChart
//
//    // global broadcast receiver so we can unregister it
//    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
//    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
//    lateinit var looperRespeck: Looper
//    lateinit var looperThingy: Looper
//
//    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
//    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)
//    lateinit var respeckModel:RespeckModel
//    lateinit var respeckInputWindow:FloatBuffer
//    lateinit var respeckMovment:String
//    lateinit var thingyMovment:String
//    var respeckNeedUpdate:Boolean = false
    lateinit var respeckButtom:Button
    lateinit var thingyButtom:Button
    lateinit var bothButtom: Button
    private var mLastClickTime: Long = 0
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)


        respeckButtom = findViewById(R.id.respeck_button)
        thingyButtom = findViewById(R.id.thingy_button)
        bothButtom = findViewById(R.id.both_button)

        respeckButtom.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            }else{
                val intent = Intent(this, RespeckActivity::class.java)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()

        }

        thingyButtom.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            }else {
                val intent = Intent(this, ThingyActivity::class.java)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }

        bothButtom.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            }else {
                val intent = Intent(this, BothActivity::class.java)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }
        respeckButtom.isEnabled = false
        thingyButtom.isEnabled = false
        bothButtom.isEnabled = false

        startSpeckService()
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
            respeckButtom.isEnabled = true
            thingyButtom.isEnabled = true
            bothButtom.isEnabled = true
        }else{
            Toast.makeText(this, "Please connect to a sensor first", Toast.LENGTH_SHORT).show()
        }
    }
}
