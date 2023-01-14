package com.specknet.pdiotapp

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.ml.RespeckModel
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.Utils
import java.nio.FloatBuffer

class RespeckActivity : AppCompatActivity() {

    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver


    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet
    var time = 0f
    lateinit var allRespeckData: LineData
    lateinit var respeckChart: LineChart
    private var predictUrl: String =  "https://pdiot-c.ew.r.appspot.com/inference"

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    lateinit var respeckInputWindow: FloatBuffer
    lateinit var respeckMovment:String
    lateinit var looperRespeck: Looper

    lateinit var respeckAccel: TextView
    lateinit var respeckGyro: TextView

    lateinit var respeckText: TextView

    lateinit var sharedPreferences: SharedPreferences
    lateinit var username: String

    lateinit var exitButton: ImageButton
    var dataReady = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respeck)

        respeckAccel = findViewById(R.id.respeck_accel)
        respeckGyro = findViewById(R.id.respeck_gyro)
        respeckText = findViewById(R.id.respeck_text)
        respeckChart = findViewById(R.id.respeck_chart)

        setupChart()
        sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()

        var respeckCounter =0
        respeckMovment = resources.getStringArray(R.array.activity_follow_model_order)[13]
//        respeckNeedUpdate = true
        respeckInputWindow = FloatBuffer.allocate(500);

        val textMessage:String = "Recognizing Action based on Respeck, please wait..."
        respeckText.text = textMessage

        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)
                    updateRespeckData(liveData)
                    // get all relevant intent contents
                    val accelX = liveData.accelX
                    val accelY = liveData.accelY
                    val accelZ = liveData.accelZ
                    var gyroX = liveData.gyro.x
                    var gyroY = liveData.gyro.y
                    var gyroZ = liveData.gyro.z
                    var fA = floatArrayOf(accelX,accelY,accelZ,gyroX,gyroY,gyroZ)
                    if(!dataReady){
                        respeckInputWindow.put(fA)
                        respeckCounter += 1
                        if(respeckCounter >= 50){
                            dataReady = true
                        }
                    }
                    if(dataReady){
                        var rArray:FloatArray = respeckInputWindow.array().sliceArray(IntRange(0,299))
                        var ccon = CloudConnection.setUpServerConnection(predictUrl);
                        var response:String = ""
                        var thr = Thread(Runnable {
                            //print(rArray)
                            print(username)
                            print(rArray)
                            response = ccon.sendRespeckDataPostRequest(username,rArray)
                        })
                        thr.start()
                        thr.join()
                        ccon.disconnect()
                        print("The action is"+response)
                        println("size: aaa  "+rArray.size)
//                        val input = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
//                        input.loadArray(rArray)
//                        val outputs = respeckModel.process(input)
//                        val output = outputs.outputFeature0AsTensorBuffer.floatArray
//                        var tempRespeckMovment = chooseBest(output)
                        runOnUiThread {
                            while (response == "") {
                            }
                           
                            val textMessage: String = String.format(
                                resources.getString(R.string.movement_text),
                                "Respack",
                                response
                            )
                            respeckText.text = textMessage
                        }
                        respeckCounter = 0
                        respeckInputWindow.clear()
                        dataReady = false
                    }

                    time += 1
                    updateGraph(accelX, accelY, accelZ)

                }
            }
        }


        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)

        exitButton = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            val intent = Intent(this, LiveDataActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun updateRespeckData(liveData: RESpeckLiveData) {
        runOnUiThread {
            respeckAccel.text =
                getString(R.string.respeck_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            respeckGyro.text =
                getString(R.string.respeck_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
        }
    }


    fun updateGraph(x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
        dataSet_res_accel_x.addEntry(Entry(time, x))
        dataSet_res_accel_y.addEntry(Entry(time, y))
        dataSet_res_accel_z.addEntry(Entry(time, z))

        runOnUiThread {
            allRespeckData.notifyDataChanged()
            respeckChart.notifyDataSetChanged()
            respeckChart.invalidate()
            respeckChart.setVisibleXRangeMaximum(150f)
            respeckChart.moveViewToX(respeckChart.lowestVisibleX + 40)
        }
    }

    fun setupChart() {

        // Respeck

        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.setColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )
        dataSet_res_accel_y.setColor(
            ContextCompat.getColor(
                this,
                R.color.green
            )
        )
        dataSet_res_accel_z.setColor(
            ContextCompat.getColor(
                this,
                R.color.blue
            )
        )

        var dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()
    }
}