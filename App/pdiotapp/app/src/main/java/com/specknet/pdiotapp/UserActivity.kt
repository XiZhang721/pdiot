package com.specknet.pdiotapp

import android.content.Context
import android.content.Intent
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
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_on_boarding.view.*

class UserActivity: AppCompatActivity() {

    lateinit var pieChart: PieChart
    private val movements = listOf( "Sitting",
        "Sitting bent forward",
        "Sitting bent backward",
        "Standing",
        "Lying down on back",
        "Lying down left",
        "Lying down right",
        "Lying down on stomach",
        "Walking at normal speed",
        "Running",
        "Climbing stairs",
        "Descending stairs",
        "General Movement",
        "Desk work")
    private val testTime = listOf(0,0,10,0,0,100,0,0,40,500,20,200,0,0)
    lateinit var logoutButton: Button
    lateinit var recordButton: Button
    lateinit var userName: TextView

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
        pieChart.description.yOffset = -10f
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
        val entries: ArrayList<PieEntry> = ArrayList()
        for(i in testTime.indices){
            if(testTime.elementAt(i)>0){
                entries.add(PieEntry(testTime.elementAt(i).toFloat(),movements.elementAt(i)))
            }
        }

        val dataSet = PieDataSet(entries,"Movements")
        dataSet.setColors(Color.LTGRAY,Color.BLUE,Color.CYAN,Color.DKGRAY,Color.GREEN,Color.MAGENTA,Color.RED,Color.YELLOW)
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