package com.specknet.pdiotapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.google.android.gms.internal.zzhu.runOnUiThread
import com.specknet.pdiotapp.ml.RespeckModel
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import kotlinx.android.synthetic.main.activity_live_data.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.FloatBuffer

/**
 * A simple [Fragment] subclass.
 * Use the [RespeckFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RespeckFragment : Fragment() {
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver


    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet
    var time = 0f
    lateinit var allRespeckData: LineData
    lateinit var respeckChart: LineChart

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    lateinit var respeckModel: RespeckModel
    lateinit var respeckInputWindow: FloatBuffer
    lateinit var respeckMovment:String
    lateinit var looperRespeck: Looper

    lateinit var respeckAccel: TextView
    lateinit var respeckGyro: TextView

    lateinit var respeckText: TextView

    var respeckNeedUpdate:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var respeck: View = inflater.inflate(R.layout.fragment_respeck, container, false)
        respeckAccel = respeck.findViewById(R.id.respeck_accel)
        respeckGyro = respeck.findViewById(R.id.respeck_gyro)
        respeckText = respeck.findViewById(R.id.respeck_text)
        respeckChart = respeck.findViewById(R.id.respeck_chart)
        // Inflate the layout for this fragment
        return respeck
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        respeckModel = RespeckModel.newInstance(requireContext())
        var respeckCounter =0
        respeckMovment = resources.getStringArray(R.array.activity_follow_model_order)[13]
        respeckNeedUpdate = true
        respeckInputWindow = FloatBuffer.allocate(500);

        val textMessage:String = String.format(resources.getString(R.string.movement_text),"Respack","General Movement")
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
                    respeckInputWindow.put(fA)
                    if(respeckCounter >= 50){
                        var rArray:FloatArray = respeckInputWindow.array().sliceArray(IntRange(0,299))

                        println("size: aaa  "+rArray.size)
                        val input = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
                        input.loadArray(rArray)
                        val outputs = respeckModel.process(input)
                        val output = outputs.outputFeature0AsTensorBuffer.floatArray
                        var tempRespeckMovment = chooseBest(output)
                        runOnUiThread{
                            val textMessage:String = String.format(resources.getString(R.string.movement_text),"Respack",tempRespeckMovment)
                            respeckText.text = textMessage}
                        respeckCounter = 0
                        respeckInputWindow.clear()

                    }
                    respeckCounter+=1

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
        requireContext().registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(respeckLiveUpdateReceiver)
        respeckModel.close()
        looperRespeck.quit()
    }

    private fun updateRespeckData(liveData: RESpeckLiveData) {
        runOnUiThread {
            respeckAccel.text = getString(R.string.respeck_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            respeckGyro.text = getString(R.string.respeck_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
        }
    }

    fun chooseBest(arr: FloatArray):String{

        var x = arr.indexOfFirst { it == arr.maxOrNull()!! }  // change name x to index
        return resources.getStringArray(R.array.activity_follow_model_order)[x];
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

        var dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
    }



}