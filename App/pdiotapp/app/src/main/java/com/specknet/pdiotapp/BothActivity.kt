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
import com.specknet.pdiotapp.utils.ThingyLiveData
import com.specknet.pdiotapp.utils.Utils
import java.nio.FloatBuffer

class BothActivity : AppCompatActivity() {
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver


    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet
    var time = 0f
    lateinit var allData: LineData
    lateinit var chart: LineChart
    private var predictUrl: String =  "https://pdiot-c.ew.r.appspot.com/inference"

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    lateinit var respeckInputWindow: FloatBuffer
    lateinit var respeckMovment:String
    lateinit var looperRespeck: Looper

    lateinit var respeckAccel: TextView
    lateinit var respeckGyro: TextView

    lateinit var bothText: TextView

    lateinit var sharedPreferences: SharedPreferences
    lateinit var username: String

    // global graph variables
    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    // global broadcast receiver so we can unregister it
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperThingy: Looper
    lateinit var thingyInputWindow: FloatBuffer
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    lateinit var thingyAccel: TextView
    lateinit var thingyGyro: TextView
    lateinit var thingyMag: TextView

    var respeckReady = false
    var thingyReady = false
    lateinit var exitButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_both)
        var isRespeckOn = false
        var isThingyOn = false

        // Set test views
        respeckAccel = findViewById(R.id.both_respeck_accel)
        respeckGyro = findViewById(R.id.both_respeck_gyro)
        thingyAccel = findViewById(R.id.both_thingy_accel)
        thingyGyro = findViewById(R.id.both_thingy_gyro)
        thingyMag = findViewById(R.id.both_thingy_mag)
        bothText = findViewById(R.id.both_text)
        chart = findViewById(R.id.both_chart)

        // Gets the username from shared preference
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()

        setupChart()
        var respeckCounter =0
        respeckMovment = resources.getStringArray(R.array.activity_follow_model_order)[13]
        respeckInputWindow = FloatBuffer.allocate(500);
        thingyInputWindow = FloatBuffer.allocate(500)
        var thingyCounter = 0
        var textMessage:String = "Recognizing Action based on both Respeck and Thingy, please wait..."
        bothText.text = textMessage

        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                    isRespeckOn = true

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

                    // Add the respeck data to input window
                    if(!respeckReady && isRespeckOn && isThingyOn){
                        respeckInputWindow.put(fA)
                        respeckCounter+=1
                        if(respeckCounter >= 50){
                            respeckReady = true
                        }
                    }

                    // if both thingy and respeck has 50 data, then send to the server for prediction
                    if(respeckReady && thingyReady){
                        //merge two input windows and send, copy and paste on the thingy part.
                        var rArray:FloatArray = respeckInputWindow.array().sliceArray(IntRange(0,299))
                        var tArray:FloatArray = thingyInputWindow.array().sliceArray(IntRange(0,449))
                        var ccon = CloudConnection.setUpServerConnection(predictUrl);
                        var response:String = ""
                        var thr = Thread(Runnable {
                            print(rArray)
                            response = ccon.sendTwoSensorDataPostRequest(username,rArray,tArray)
                        })
                        thr.start()
                        thr.join()
                        ccon.disconnect()

                        // Update the prediction text
                        runOnUiThread {
                            while (response == "") {}
                            val textMessage: String = String.format(
                                resources.getString(R.string.movement_both_text),
                                response
                            )
                            bothText.text = textMessage
                        }

                        // Clear the windows
                        respeckCounter = 0
                        thingyCounter = 0
                        respeckInputWindow.clear()
                        thingyInputWindow.clear()
                        respeckReady = false
                        thingyReady = false
                    }
                    time += 0.5f
                    updateRespeckGraph(accelX, accelY, accelZ)
                }
            }
        }

        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive2")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)

        thingyLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_THINGY_BROADCAST) {
                    isThingyOn = true
                    val liveData =
                        intent.getSerializableExtra(Constants.THINGY_LIVE_DATA) as ThingyLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)
                    updateThingyData(liveData)
                    // get all relevant intent contents
                    val accelX = liveData.accelX
                    val accelY = liveData.accelY
                    val accelZ = liveData.accelZ
                    val gyroX = liveData.gyro.x
                    val gyroY = liveData.gyro.y
                    val gyroZ = liveData.gyro.z
                    val magX = liveData.mag.x
                    val magY = liveData.mag.y
                    val magZ = liveData.mag.z
                    var fA = floatArrayOf(accelX,accelY,accelZ,gyroX,gyroY,gyroZ,magX,magY,magZ)

                    // Add the thingy data to input window
                    if(!thingyReady&& isRespeckOn && isThingyOn){
                        thingyInputWindow.put(fA)
                        thingyCounter += 1
                        if(thingyCounter >= 50){
                            thingyReady = true
                        }
                    }
                    time += 0.5f
                    updateThingyGraph(accelX,accelY,accelZ)

                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive2")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)

        // Set the exit buttom
        exitButton = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            val intent = Intent(this, LiveDataActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * This function updates the respeck data text.
     */
    private fun updateRespeckData(liveData: RESpeckLiveData) {
        runOnUiThread {
            respeckAccel.text =
                getString(R.string.both_respeck_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            respeckGyro.text =
                getString(R.string.both_respeck_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
        }
    }

    /**
     * This function updates the respeck data graph.
     */
    fun updateRespeckGraph(x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
        dataSet_res_accel_x.addEntry(Entry(time, x))
        dataSet_res_accel_y.addEntry(Entry(time, y))
        dataSet_res_accel_z.addEntry(Entry(time, z))

        runOnUiThread {
            allData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
            chart.setVisibleXRangeMaximum(150f)
            chart.moveViewToX(chart.lowestVisibleX + 40)
        }
    }

    /**
     * This function updates the thingy data graph.
     */
    private fun updateThingyGraph(x: Float, y: Float, z: Float) {
        dataSet_thingy_accel_x.addEntry(Entry(time, x))
        dataSet_thingy_accel_y.addEntry(Entry(time, y))
        dataSet_thingy_accel_z.addEntry(Entry(time, z))

        runOnUiThread {
            allData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
            chart.setVisibleXRangeMaximum(150f)
            chart.moveViewToX(chart.lowestVisibleX + 40)
        }
    }

    /**
     * This function updates the thingy data text.
     */
    private fun updateThingyData(liveData: ThingyLiveData) {
        // update UI thread
        runOnUiThread {
            thingyAccel.text =
                getString(R.string.both_thingy_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            thingyGyro.text =
                getString(R.string.both_thingy_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
            thingyMag.text =
                getString(R.string.both_thingy_mag, liveData.mag.x, liveData.mag.y, liveData.mag.z)
        }
    }

    /**
     * This function sets up the chart for live data of both respeck and thingy.
     */
    private fun setupChart() {

        // Respeck

        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Respeck Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Respeck Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Respeck Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        val entries_thingy_accel_x = ArrayList<Entry>()
        val entries_thingy_accel_y = ArrayList<Entry>()
        val entries_thingy_accel_z = ArrayList<Entry>()

        dataSet_thingy_accel_x = LineDataSet(entries_thingy_accel_x, "Thingy Accel X")
        dataSet_thingy_accel_y = LineDataSet(entries_thingy_accel_y, "Thingy Accel Y")
        dataSet_thingy_accel_z = LineDataSet(entries_thingy_accel_z, "Thingy Accel Z")

        dataSet_thingy_accel_x.setDrawCircles(false)
        dataSet_thingy_accel_y.setDrawCircles(false)
        dataSet_thingy_accel_z.setDrawCircles(false)

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

        dataSet_thingy_accel_x.setColor(
            ContextCompat.getColor(
                this,
                R.color.yellow
            )
        )
        dataSet_thingy_accel_y.setColor(
            ContextCompat.getColor(
                this,
                R.color.orange
            )
        )
        dataSet_thingy_accel_z.setColor(
            ContextCompat.getColor(
                this,
                R.color.purple
            )
        )

        var dataSets= ArrayList<ILineDataSet>()
        dataSets.add(dataSet_res_accel_x)
        dataSets.add(dataSet_res_accel_y)
        dataSets.add(dataSet_res_accel_z)
        dataSets.add(dataSet_thingy_accel_x)
        dataSets.add(dataSet_thingy_accel_y)
        dataSets.add(dataSet_thingy_accel_z)

        allData = LineData(dataSets)
        chart.data = allData
        chart.legend.isWordWrapEnabled = true
        chart.invalidate()
    }

    /**
     * This function disables the back key
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        unregisterReceiver(thingyLiveUpdateReceiver)
        looperThingy.quit()
        looperRespeck.quit()
    }
}