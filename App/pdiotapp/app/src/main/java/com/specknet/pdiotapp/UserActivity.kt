package com.specknet.pdiotapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.Utils

class UserActivity: AppCompatActivity() {

    lateinit var pieChart: PieChart
    /*
        This movement list is in the order of the list stored in the server
     */
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

    private var hitoricalUrl: String =  "https://pdiot-c.ew.r.appspot.com/history"
    private var stepUrl:String = "https://pdiot-c.ew.r.appspot.com/step"
    lateinit var logoutButton: Button
    lateinit var recordButton: Button
    lateinit var userName: TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var username: String
    lateinit var stepcount: TextView
    var step = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        logoutButton = findViewById(R.id.logout_button)
        recordButton = findViewById(R.id.record_button)

        userName = findViewById(R.id.username_text)

        // Gets the username from shared preference
        var sharedPreferences =  getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        userName.text = sharedPreferences.getString(Constants.USERNAME_PREF,"")

        setupButtons()
        configChartView()
        getStepCount()

        // Sets the bottom navigator
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

    /**
     * This function gets the step count from the server and update the text.
     */
    private fun getStepCount() {
        stepcount = findViewById(R.id.step_count_text)
        step = 0
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()
        var stepCcon = CloudConnection.setUpStepConnection(stepUrl)
        var response = "";
        var thr = Thread(Runnable {
            response = stepCcon.sendStepCountPostRequest(username)
        })
        thr.start()
        thr.join()
        stepCcon.disconnect()
        stepcount.text = String.format(resources.getString(R.string.step_text),response)
    }

    /**
     * This function sets up the record button and the logout button
     */
    private fun setupButtons() {
        recordButton.setOnClickListener {
            val intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            stopSpeckService()
            val intent = Intent(this, MainActivity::class.java)

            // Add flags to clear historical activity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }
    }

    /**
     * This function disconnects all sensors.
     */
    private fun stopSpeckService(){
        val isServiceRunning = Utils.isServiceRunning(BluetoothSpeckService::class.java, applicationContext)
        if(isServiceRunning){
            this.stopService(Intent(this, BluetoothSpeckService::class.java))
        }
    }

    /**
     * This function gets historical data from the server and create pie chart based on the historical data
     */
    private fun configChartView() {
        // Pie chart settings
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

        // Get the user name from the shared preference
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Constants.USERNAME_PREF,"").toString()

        // Get the historical data from the server
        var ccon = CloudConnection.setUpHistoricalConnection(hitoricalUrl)
        var response = "";
        var thr = Thread(Runnable {
            response = ccon.sendHistoricalPostRequest(username)
        })
        thr.start()
        thr.join()
        ccon.disconnect()
        var gson:Gson = Gson()
        var historical = gson.fromJson(response,IntArray::class.java)

        // Add the data to entries
        val entries: ArrayList<PieEntry> = ArrayList()
        for(i in historical.indices){
            if(historical.elementAt(i)>0){
                var sec = historical.elementAt(i).toFloat() * 2
                entries.add(PieEntry(sec,movements.elementAt(i)))
            }
        }

        // Sets up the pie chart
        val dataSet = PieDataSet(entries,"")
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