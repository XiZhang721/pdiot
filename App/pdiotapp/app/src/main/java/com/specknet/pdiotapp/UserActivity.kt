package com.specknet.pdiotapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_on_boarding.view.*

class UserActivity: AppCompatActivity() {

    lateinit var pieChart: PieChart
    private val movements = listOf(
        "Desk work",
        "Walking at normal speed",
        "Climbing stairs",
        "Descending stairs",
        "Sitting",
        "Sitting bent forward",
        "Sitting bent backward",
        "Standing",
        "Running",
        "Lying down left",
        "Lying down right",
        "Lying down on back",
        "Lying down on stomach",
        "General Movement"
    )
//    private val testTime = listOf(0,0,10,0,0,100,0,0,40,500,20,200,0,0)
    private lateinit var actualHistoricalData:FloatArray;
    private lateinit var ccon: CloudConnection;
    private var hitoricalUrl: String =  "https://pdiot-c.ew.r.appspot.com/history"
    lateinit var logoutButton: Button
    lateinit var recordButton: Button
    lateinit var userName: TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        logoutButton = findViewById(R.id.logout_button)
        recordButton = findViewById(R.id.record_button)

        userName = findViewById(R.id.username_text)
        var sharedPreferences =  getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        userName.text = sharedPreferences.getString(Constants.USERNAME_PREF,"")

        setupButtons()
        configChartView()

        var bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.user
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.connection ->{
                    val intent = Intent(this, MainConnectActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    true
                }
                R.id.live->{
                    val intent = Intent(this, LiveDataActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    true
                }
                R.id.user->{
                    true
                }
            }
            false
        }
    }

    private fun setupButtons() {
        recordButton.setOnClickListener {
            val intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            stopSpeckService()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }
    }

    private fun stopSpeckService(){
        val isServiceRunning = Utils.isServiceRunning(BluetoothSpeckService::class.java, applicationContext)
        if(isServiceRunning){
            this.stopService(Intent(this, BluetoothSpeckService::class.java))
        }
    }

    private fun configChartView() {
        pieChart = findViewById(R.id.history_chart)
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = true
        pieChart.description.text = "Unit: second"
        pieChart.description.xOffset = 15f
        pieChart.description.yOffset = -20f
        pieChart.description.textSize = 13f
        pieChart.setExtraOffsets(5f, 10f, 5f, 10f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.transparentCircleRadius = 0f
        pieChart.holeRadius = 0f
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.legend.isEnabled = true
        pieChart.setDrawEntryLabels(false)
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()
        ccon = CloudConnection.setUpHistoricalConnection(hitoricalUrl)
        var response = "";
        var thr = Thread(Runnable {
            response = ccon.sendHistoricalPostRequest(username)
        })
        thr.start()
        while (response == ""){}
        thr.interrupt()
        ccon.disconnect()
        var gson:Gson = Gson()
        var historical = gson.fromJson(response,IntArray::class.java)

        val entries: ArrayList<PieEntry> = ArrayList()
        for(i in historical.indices){
            if(historical.elementAt(i)>0){
                var sec = historical.elementAt(i).toFloat() * 2
                entries.add(PieEntry(sec,movements.elementAt(i)))
            }
        }

        val dataSet = PieDataSet(entries,"Movements")
        dataSet.setColors(
            resources.getColor(R.color.chart_color1),
            resources.getColor(R.color.chart_color2),
            resources.getColor(R.color.chart_color3),
            resources.getColor(R.color.chart_color4),
            resources.getColor(R.color.chart_color5),
            resources.getColor(R.color.chart_color6),
            resources.getColor(R.color.chart_color7),
            resources.getColor(R.color.chart_color8),
            resources.getColor(R.color.chart_color9),
            resources.getColor(R.color.chart_color10),
            resources.getColor(R.color.chart_color11),
            resources.getColor(R.color.chart_color12),
            resources.getColor(R.color.chart_color13),
            resources.getColor(R.color.chart_color14)
            )
        dataSet.sliceSpace = 0f

        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1Length = 1.25f
        val data = PieData(dataSet)

        data.setValueTextSize(13f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.BLACK)
        pieChart.data = data
        pieChart.legend.setDrawInside(false)
        pieChart.legend.isWordWrapEnabled = true
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }


}