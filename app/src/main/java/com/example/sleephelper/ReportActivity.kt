package com.example.sleephelper

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.sleephelper.databinding.ActivityReportBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ReportActivity : AppCompatActivity(), View.OnClickListener {


    private var binding: ActivityReportBinding? = null
    private var chart : LineChart? = null
    private var reportDataList : ArrayList<ReportData>? = null
    private var db = FirebaseFirestore.getInstance()
    private var dbDataList : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        reportDataList = ReportModel.defaultReportDataList()

        setChart()

        setBottomNavigation()

        setFabAdd()

        setUpWeeklyView()

        //firebase test
        //db.collection("")


        //

        //리스너 달기
        binding?.btnWeekly?.setOnClickListener(this)
        binding?.btnMonthly?.setOnClickListener(this)
        binding?.clScore?.setOnClickListener(this)
    }

    private fun setFabAdd(){
        binding!!.fabAdd.setOnClickListener(){
            intent = Intent(this, WritingDiaryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setBottomNavigation(){
        binding!!.bottomNavigation.setOnItemSelectedListener(){
            when(it.itemId){
                R.id.nav_calendar -> {
                    intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_alarm -> {
                    intent = Intent(this, RecommendTimeActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_myPage -> {
                    intent = Intent(this, MypageActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun setChart(){
        chart = binding?.sleepLineChart


        val values = ArrayList<Entry>()

        for(i in 1..100){
            var value : Float = Math.random().toFloat()
            values.add(Entry(i.toFloat(), value))
        }

        val set1 = LineDataSet(values, "Sleep Chart")

        val dataSets:ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)

        val data : LineData = LineData(dataSets)

        chart?.legend?.textColor = Color.parseColor("#FCA311")
        chart?.xAxis?.textColor = Color.parseColor("#E5E5E5")
        chart?.axisLeft?.textColor = Color.parseColor("#E5E5E5")
        chart?.axisRight?.textColor = Color.parseColor("#14213D")

        set1.setColor(Color.parseColor("#FCA311"))
        set1.setCircleColor(Color.parseColor("#FCA311"))
        set1.setDrawCircles(false)
        set1.setDrawValues(false)

        chart?.setData(data)
    }


    //오늘 날짜 가져오기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate():String{
        val current = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formatted = current.format(formatter)

        return formatted
    }

    //분단위 시간으로 바꾸기
    private fun calTimeInMins(time : String):Int{
        val arr = time.split(":")
        val hour = arr[0].toInt()
        val minute = arr[1].toInt()

        return hour*60+minute
    }

    //분단위 시간을 hh:mm 으로 바꾸기
    private fun calTime(time : Int):String{
        val df = DecimalFormat("00")
        val hour = df.format(time/60).toString()
        val minute = df.format(time%60).toString()

        return "$hour:$minute"
    }

    //오늘 날짜에서 1일 씩 빼기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calDate() {
        var current = getDate().toInt()
        if(current%100 > 0){
            current--
        }else{
            current-100+30
        }
    }

    //firestore에서 데이터가져와서 로컬 db에 저장
    private fun getDbDataList(){

    }



    private fun calWeeklyAverage(){
        for(i in 0..6){

        }

    }

    private fun calMonthlyAverage(){

    }

    private fun setUpWeeklyView(){
        binding?.btnWeekly?.background = getDrawable(R.drawable.report_selected_left_button_background)
        binding?.btnMonthly?.background = getDrawable(R.drawable.report_right_button_background)
        binding?.tvScore?.text = reportDataList?.get(0)?.weeklyAverage
        binding?.tvTimeInBed?.text = reportDataList?.get(1)?.weeklyAverage
        binding?.tvSleepTime?.text = reportDataList?.get(2)?.weeklyAverage
        binding?.tvBI?.text = reportDataList?.get(3)?.weeklyAverage
        binding?.tvBedTime?.text = reportDataList?.get(4)?.weeklyAverage
        binding?.tvWakeUpTime?.text = reportDataList?.get(5)?.weeklyAverage
        binding?.tvNapTime?.text = reportDataList?.get(6)?.weeklyAverage
        binding?.tvCoffee?.text = reportDataList?.get(7)?.weeklyAverage
        binding?.tvBeer?.text = reportDataList?.get(8)?.weeklyAverage
        binding?.tvSoju?.text = reportDataList?.get(9)?.weeklyAverage
        binding?.tvMakgurli?.text = reportDataList?.get(10)?.weeklyAverage
        binding?.tvWine?.text = reportDataList?.get(11)?.weeklyAverage
    }

    private fun setUpMonthlyView(){
        binding?.btnWeekly?.background = getDrawable(R.drawable.report_left_button_background)
        binding?.btnMonthly?.background = getDrawable(R.drawable.report_selected_right_button_background)
        binding?.tvScore?.text = reportDataList?.get(0)?.monthlyAverage
        binding?.tvTimeInBed?.text = reportDataList?.get(1)?.monthlyAverage
        binding?.tvSleepTime?.text = reportDataList?.get(2)?.monthlyAverage
        binding?.tvBI?.text = reportDataList?.get(3)?.monthlyAverage
        binding?.tvBedTime?.text = reportDataList?.get(4)?.monthlyAverage
        binding?.tvWakeUpTime?.text = reportDataList?.get(5)?.monthlyAverage
        binding?.tvNapTime?.text = reportDataList?.get(6)?.monthlyAverage
        binding?.tvCoffee?.text = reportDataList?.get(7)?.monthlyAverage
        binding?.tvBeer?.text = reportDataList?.get(8)?.monthlyAverage
        binding?.tvSoju?.text = reportDataList?.get(9)?.monthlyAverage
        binding?.tvMakgurli?.text = reportDataList?.get(10)?.monthlyAverage
        binding?.tvWine?.text = reportDataList?.get(11)?.monthlyAverage
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_weekly -> {

                setUpWeeklyView()
            }
            R.id.btn_monthly -> {
                setUpMonthlyView()
            }
            R.id.clScore -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
