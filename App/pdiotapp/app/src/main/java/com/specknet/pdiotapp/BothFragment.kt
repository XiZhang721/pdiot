package com.specknet.pdiotapp

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.internal.zzhu
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.ml.RespeckModel
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.ThingyLiveData
import java.nio.FloatBuffer

/**
 * A simple [Fragment] subclass.
 * Use the [BothFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BothFragment : Fragment() {
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver


    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet
    var time = 0f
    lateinit var allData: LineData
    lateinit var chart: LineChart
    private var predictUrl: String =  "https://pdiot-c.ew.r.appspot.com/inference"
    private lateinit var ccon: CloudConnection

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    lateinit var respeckModel: RespeckModel
    lateinit var respeckInputWindow: FloatBuffer
    lateinit var respeckMovment:String
    lateinit var looperRespeck: Looper

    lateinit var respeckAccel: TextView
    lateinit var respeckGyro: TextView

    lateinit var bothText: TextView

    lateinit var thr: Thread
    lateinit var sharedPreferences: SharedPreferences
    lateinit var username: String
    //    var respeckNeedUpdate:Boolean = false
    // global graph variables
    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    // global broadcast receiver so we can unregister it
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperThingy: Looper
    lateinit var thingyInputWindow: FloatBuffer
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    lateinit var thingyAccel:TextView
    lateinit var thingyGyro:TextView
    lateinit var thingyMag:TextView
    lateinit var thingyText: TextView

    var respeckReady = false
    var thingyReady = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var both: View = inflater.inflate(R.layout.fragment_respeck, container, false)
        respeckAccel = both.findViewById(R.id.both_respeck_accel)
        respeckGyro = both.findViewById(R.id.both_respeck_gyro)
        thingyAccel = both.findViewById(R.id.both_thingy_accel)
        thingyGyro = both.findViewById(R.id.both_thingy_gyro)
        thingyMag = both.findViewById(R.id.both_thingy_mag)
        bothText = both.findViewById(R.id.both_text)
        chart = both.findViewById(R.id.both_chart)
        // Inflate the layout for this fragment
        return both
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        sharedPreferences = requireContext().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()

        respeckModel = RespeckModel.newInstance(requireContext())
        var respeckCounter =0
        respeckMovment = resources.getStringArray(R.array.activity_follow_model_order)[13]
//        respeckNeedUpdate = true
        respeckInputWindow = FloatBuffer.allocate(500);

        var textMessage:String = String.format(resources.getString(R.string.movement_both_text),"General Movement")
        bothText.text = textMessage

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
                    if(!respeckReady){
                        respeckInputWindow.put(fA)
                        if(respeckCounter >= 50){
                            respeckReady = true
                        }
                    }
                    if(respeckReady && thingyReady){

                        //merge two input windows and send, copy and paste on the thingy part.


                        var rArray:FloatArray = respeckInputWindow.array().sliceArray(IntRange(0,299))
                        ccon = CloudConnection.setUpServerConnection(predictUrl);
                        var response:String = ""
                        thr = Thread(Runnable {
                            print(rArray)
                            response = ccon.sendRespeckDataPostRequest(username,rArray)
                            Thread.sleep(500)
                        })
                        thr.start()
                        ccon.disconnect()
                        print("The action is"+response)
                        println("size: aaa  "+rArray.size)
//                        val input = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
//                        input.loadArray(rArray)
//                        val outputs = respeckModel.process(input)
//                        val output = outputs.outputFeature0AsTensorBuffer.floatArray
//                        var tempRespeckMovment = chooseBest(output)
                        zzhu.runOnUiThread {
                            while (response == "") {
                            }
                            val textMessage: String = String.format(
                                resources.getString(R.string.movement_text),
                                "Respack",
                                response
                            )
                            bothText.text = textMessage
                        }
                        respeckCounter = 0
                        respeckInputWindow.clear()

                    }
                    respeckCounter+=1

                    time += 1
                    updateRespeckGraph(accelX, accelY, accelZ)
                    respeckReady = false
                    thingyReady = false

                }
            }
        }

        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        requireContext().registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)
        thingyText.text = textMessage
        thingyInputWindow = FloatBuffer.allocate(500)
        var thingyCounter = 0
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
                    if(!thingyReady){
                        thingyInputWindow.put(fA)
                        if(thingyCounter >= 50){
                            thingyReady = true
                        }
                    }
                    if(respeckReady && thingyReady){
                        var rArray:FloatArray = thingyInputWindow.array().sliceArray(IntRange(0,449))
                        ccon = CloudConnection.setUpServerConnection(predictUrl);
                        var response:String = ""
                        thr = Thread(Runnable {
                            print(rArray)
                            response = ccon.sendThingyDataPostRequest(username,rArray)
                            Thread.sleep(500)
                        })
                        thr.start()
                        ccon.disconnect()
                        print("The action is"+response)
                        zzhu.runOnUiThread {
                            while (response == "") {
                            }
                            val textMessage: String = String.format(
                                resources.getString(R.string.movement_text),
                                "Respack",
                                response
                            )
                            thingyText.text = textMessage
                        }
                        thingyCounter = 0
                        thingyInputWindow.clear()
                    }


                    time += 1
                    thingyCounter += 1
                    updateThingyGraph(accelX,accelY,accelZ)
                    respeckReady = false
                    thingyReady = false
                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        requireContext().registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(respeckLiveUpdateReceiver)
        thr.interrupt()
        respeckModel.close()
        looperRespeck.quit()
    }

    private fun updateRespeckData(liveData: RESpeckLiveData) {
        zzhu.runOnUiThread {
            respeckAccel.text =
                getString(R.string.both_respeck_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            respeckGyro.text =
                getString(R.string.both_respeck_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
        }
    }

    fun chooseBest(arr: FloatArray):String{

        var x = arr.indexOfFirst { it == arr.maxOrNull()!! }  // change name x to index
        return resources.getStringArray(R.array.activity_follow_model_order)[x];
    }

    fun updateRespeckGraph(x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
        dataSet_res_accel_x.addEntry(Entry(time, x))
        dataSet_res_accel_y.addEntry(Entry(time, y))
        dataSet_res_accel_z.addEntry(Entry(time, z))

        zzhu.runOnUiThread {
            allData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
            chart.setVisibleXRangeMaximum(150f)
            chart.moveViewToX(chart.lowestVisibleX + 40)
        }
    }

    private fun updateThingyGraph(x: Float, y: Float, z: Float) {
        dataSet_thingy_accel_x.addEntry(Entry(time, x))
        dataSet_thingy_accel_y.addEntry(Entry(time, y))
        dataSet_thingy_accel_z.addEntry(Entry(time, z))

        zzhu.runOnUiThread {
            allData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
            chart.setVisibleXRangeMaximum(150f)
            chart.moveViewToX(chart.lowestVisibleX + 40)
        }
    }

    private fun updateThingyData(liveData: ThingyLiveData) {
        // update UI thread
        zzhu.runOnUiThread {
            thingyAccel.text =
                getString(R.string.both_thingy_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            thingyGyro.text =
                getString(R.string.both_thingy_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
            thingyMag.text =
                getString(R.string.both_thingy_mag, liveData.mag.x, liveData.mag.y, liveData.mag.z)
        }
    }

    fun setupChart() {

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
                requireContext(),
                R.color.red
            )
        )
        dataSet_res_accel_y.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.green
            )
        )
        dataSet_res_accel_z.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.blue
            )
        )

        dataSet_thingy_accel_x.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.yellow
            )
        )
        dataSet_thingy_accel_y.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.orange
            )
        )
        dataSet_thingy_accel_z.setColor(
            ContextCompat.getColor(
                requireContext(),
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
        chart.invalidate()
    }
}