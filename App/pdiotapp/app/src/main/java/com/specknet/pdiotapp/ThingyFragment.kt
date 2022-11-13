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
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.ThingyLiveData


/**
 * A simple [Fragment] subclass.
 * Use the [ThingyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ThingyFragment : Fragment() {
    // global graph variables
    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    var time = 0f
    lateinit var allThingyData: LineData
    lateinit var thingyChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperThingy: Looper
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    lateinit var thingyAccel:TextView
    lateinit var thingyGyro:TextView
    lateinit var thingyMag:TextView
    lateinit var thingyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var thingy: View = inflater.inflate(R.layout.fragment_thingy, container, false)
        thingyAccel = thingy.findViewById(R.id.thingy_accel)
        thingyGyro = thingy.findViewById(R.id.thingy_gyro)
        thingyMag = thingy.findViewById(R.id.thingy_mag)
        thingyChart = thingy.findViewById(R.id.thingy_chart)
        thingyText = thingy.findViewById(R.id.thingy_text)
        // Inflate the layout for this fragment
        return thingy
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        setupChart()
        val textMessage:String = String.format(resources.getString(R.string.movement_text),"Thingy","General Movement")
        thingyText.text = textMessage
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
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    time += 1
                    updateGraph(x, y, z)

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

    private fun updateThingyData(liveData: ThingyLiveData) {
        // update UI thread
        runOnUiThread {
            thingyAccel.text = getString(R.string.thingy_accel, liveData.accelX, liveData.accelY, liveData.accelZ)
            thingyGyro.text = getString(R.string.thingy_gyro, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z)
            thingyMag.text = getString(R.string.thingy_mag, liveData.mag.x, liveData.mag.y, liveData.mag.z)
        }
    }

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
                requireContext(),
                R.color.red
            )
        )
        dataSet_thingy_accel_y.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.green
            )
        )
        dataSet_thingy_accel_z.setColor(
            ContextCompat.getColor(
                requireContext(),
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


    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(thingyLiveUpdateReceiver)
        looperThingy.quit()
    }
}