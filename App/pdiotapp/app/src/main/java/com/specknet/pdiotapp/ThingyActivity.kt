package com.specknet.pdiotapp

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.ThingyLiveData
import java.nio.FloatBuffer

class ThingyActivity : AppCompatActivity() {
    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    var time = 0f
    lateinit var allThingyData: LineData
    lateinit var thingyChart: LineChart
    private var predictUrl: String =  "https://pdiot-c.ew.r.appspot.com/inference"
    // global broadcast receiver so we can unregister it
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperThingy: Looper
    lateinit var thingyInputWindow: FloatBuffer
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    lateinit var thingyAccel: TextView
    lateinit var thingyGyro: TextView
    lateinit var thingyMag: TextView
    lateinit var thingyText: TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var username: String
    var dataReady = false
    lateinit var exitButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thingy)

        thingyAccel = findViewById(R.id.thingy_accel)
        thingyGyro = findViewById(R.id.thingy_gyro)
        thingyMag = findViewById(R.id.thingy_mag)
        thingyChart = findViewById(R.id.thingy_chart)
        thingyText = findViewById(R.id.thingy_text)

        setupChart()

        // Gets the username from shared preference
        sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()

        val textMessage:String = "Recognizing Action based on Thingy, please wait..."
        thingyText.text = textMessage
        thingyInputWindow = FloatBuffer.allocate(500)
        var thingyCounter = 0

        // set up the broadcast receiver
        thingyLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_THINGY_BROADCAST) {

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
                    if(!dataReady){
                        thingyInputWindow.put(fA)
                        thingyCounter += 1
                        if(thingyCounter >= 50){
                            dataReady = true
                        }
                    }

                    // if thingy has 50 data, then send to the server for prediction
                    if(dataReady){
                        var rArray:FloatArray = thingyInputWindow.array().sliceArray(IntRange(0,449))
                        var ccon = CloudConnection.setUpServerConnection(predictUrl);
                        var response:String = ""
                        var thr = Thread(Runnable {
                            print(rArray)
                            response = ccon.sendThingyDataPostRequest(username,rArray)
                        })
                        thr.start()
                        thr.join()
                        ccon.disconnect()

                        // Update the prediction text
                        runOnUiThread {
                            val textMessage: String = String.format(
                                resources.getString(R.string.movement_text),
                                "Thingy",
                                response
                            )
                            thingyText.text = textMessage
                        }

                        // Clear the windows
                        thingyCounter = 0
                        thingyInputWindow.clear()
                        dataReady = false
                    }
                    time += 1
                    updateGraph(accelX,accelY,accelZ)

                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)

        // Set up the exit button
        exitButton = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            val intent = Intent(this, LiveDataActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * This function updates the thingy data graph.
     */
    private fun updateGraph(x: Float, y: Float, z: Float) {
        dataSet_thingy_accel_x.addEntry(Entry(time, x))
        dataSet_thingy_accel_y.addEntry(Entry(time, y))
        dataSet_thingy_accel_z.addEntry(Entry(time, z))

        runOnUiThread {
            allThingyData.notifyDataChanged()
            thingyChart.notifyDataSetChanged()
            thingyChart.invalidate()
            thingyChart.setVisibleXRangeMaximum(150f)
            thingyChart.moveViewToX(thingyChart.lowestVisibleX + 40)
        }
    }

    /**
     * This function updates the thingy data texts.
     */
    private fun updateThingyData(liveData: ThingyLiveData) {
        // update UI thread
        runOnUiThread {
            thingyAccel.text =
                getString(R.string.thingy_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            thingyGyro.text =
                getString(R.string.thingy_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
            thingyMag.text =
                getString(R.string.thingy_mag, liveData.mag.x, liveData.mag.y, liveData.mag.z)
        }
    }

    /**
     * This function sets up the chart for live data of thingy.
     */
    private fun setupChart() {
        // Thingy

        time = 0f
        val entries_thingy_accel_x = ArrayList<Entry>()
        val entries_thingy_accel_y = ArrayList<Entry>()
        val entries_thingy_accel_z = ArrayList<Entry>()

        dataSet_thingy_accel_x = LineDataSet(entries_thingy_accel_x, "Accel X")
        dataSet_thingy_accel_y = LineDataSet(entries_thingy_accel_y, "Accel Y")
        dataSet_thingy_accel_z = LineDataSet(entries_thingy_accel_z, "Accel Z")

        dataSet_thingy_accel_x.setDrawCircles(false)
        dataSet_thingy_accel_y.setDrawCircles(false)
        dataSet_thingy_accel_z.setDrawCircles(false)

        dataSet_thingy_accel_x.setColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )
        dataSet_thingy_accel_y.setColor(
            ContextCompat.getColor(
                this,
                R.color.green
            )
        )
        dataSet_thingy_accel_z.setColor(
            ContextCompat.getColor(
                this,
                R.color.blue
            )
        )

        val dataSetsThingy = ArrayList<ILineDataSet>()
        dataSetsThingy.add(dataSet_thingy_accel_x)
        dataSetsThingy.add(dataSet_thingy_accel_y)
        dataSetsThingy.add(dataSet_thingy_accel_z)

        allThingyData = LineData(dataSetsThingy)
        thingyChart.data = allThingyData
        thingyChart.invalidate()
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
        unregisterReceiver(thingyLiveUpdateReceiver)
        looperThingy.quit()
    }
}