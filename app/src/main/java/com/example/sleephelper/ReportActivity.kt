package com.example.sleephelper

import android.content.Intent
import android.graphics.Color
import android.nfc.Tag
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.sleephelper.ChartData
import com.example.sleephelper.databinding.ActivityReportBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

const val dayInMins = 1440
const val week = 7
const val month = 30

enum class Duration(val duration : Int){
    WEEK(6),
    MONTH(29)
}

@RequiresApi(Build.VERSION_CODES.O)
class ReportActivity : AppCompatActivity(), View.OnClickListener, CoroutineScope {

    private var duration : Int? = null
    private var binding: ActivityReportBinding? = null
    private var chart: BarChart? = null


    private var reportDataList: ArrayList<ReportData>? = null
    private var db = FirebaseFirestore.getInstance()

    //각 항목 들의 데이터를 30일 치 담는 리스트
    private var dbDataList: ArrayList<String>? = null

    private var bedEndTimeList: kotlin.collections.ArrayList<String>? = null
    private var bedStartTimeList: kotlin.collections.ArrayList<String>? = null
    private var beerList: kotlin.collections.ArrayList<String>? = null
    private var coffeeList: kotlin.collections.ArrayList<String>? = null
    private var makgeolliList: kotlin.collections.ArrayList<String>? = null
    private var napTimeList: kotlin.collections.ArrayList<String>? = null
    private var sojuList: kotlin.collections.ArrayList<String>? = null
    private var startSleepTimeList: kotlin.collections.ArrayList<String>? = null
    private var wakeUpTimeList: kotlin.collections.ArrayList<String>? = null
    private var wineList: kotlin.collections.ArrayList<String>? = null
    private var wakeUpCountList: kotlin.collections.ArrayList<String>? = null
    private var emojiList: kotlin.collections.ArrayList<String>? = null

    private var scoreList : kotlin.collections.ArrayList<com.example.sleephelper.ChartData>? = null
    private var bedTimeList : ArrayList<com.example.sleephelper.ChartData>? = null
    private var sleepTimeList : kotlin.collections.ArrayList<com.example.sleephelper.ChartData>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        reportDataList = ReportModel.defaultReportDataList()

        init()

        setBottomNavigation()
        setFabAdd()

        //리스너 달기
        binding?.btnWeekly?.setOnClickListener(this)
        binding?.btnMonthly?.setOnClickListener(this)
        binding?.clScore?.setOnClickListener(this)
        binding?.clTimeInBed?.setOnClickListener(this)
        binding?.clSleepTime?.setOnClickListener(this)
    }

    private fun setFabAdd() {
        binding!!.fabAdd.setOnClickListener() {
            intent = Intent(this, WritingDiaryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setBottomNavigation() {
        binding!!.bottomNavigation.setOnItemSelectedListener() {
            when (it.itemId) {
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

    inner class WeeklyValueFormatter:ValueFormatter(){
        val dateList = kotlin.collections.ArrayList<String>()

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            for(i in 0..Duration.WEEK.duration){
                if(bedTimeList?.get(i)?.date != "") {
                    dateList.add(changeDateFormat(bedTimeList?.get(i)?.date!!))
                }else{
                    dateList.add("")
                }
            }
            return dateList.getOrNull(value.toInt()-1)?:value.toString()
        }
    }

    inner class MonthlyValueFormatter:ValueFormatter(){
        val dateList = kotlin.collections.ArrayList<String>()

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            for(i in 0..Duration.MONTH.duration){
                if(bedTimeList?.get(i)?.date != "") {
                    dateList.add(changeDateFormat(bedTimeList?.get(i)?.date!!))
                }else{
                    dateList.add("")
                }
            }
            return dateList.getOrNull(value.toInt()-1)?:value.toString()
        }
    }

    //차트 데이터 바꾸기
    private fun changeChartData(dataList:kotlin.collections.ArrayList<com.example.sleephelper.ChartData>, duration : Int):ArrayList<BarEntry>{
        var value = 0
        var date :String? = null
        val values = ArrayList<BarEntry>()
       // dataList?.reverse()
        if(duration == Duration.WEEK.duration){
            for(i in 0..Duration.WEEK.duration){
                value = dataList?.get(i)?.value!!
                //date = dataList?.get(i)?.date
                chart?.xAxis?.valueFormatter = WeeklyValueFormatter()
                //Log.d("score",value.toString())
                values.add(BarEntry(i.toFloat(),value.toFloat()))
                chart?.notifyDataSetChanged();
                chart?.invalidate();
            }
        }else if(duration == Duration.MONTH.duration){
            for(i in 0..Duration.MONTH.duration){
                value = dataList?.get(i)?.value!!
                //date = dataList?.get(i)?.date
                chart?.xAxis?.valueFormatter = MonthlyValueFormatter()
                //Log.d("score",value.toString())
                values.add(BarEntry(i.toFloat(),value.toFloat()))
                chart?.notifyDataSetChanged();
                chart?.invalidate();
            }
        }
//        for(i in 0..29){
//            value = dataList?.get(i)!!
//            //Log.d("score",value.toString())
//            values.add(BarEntry(i.toFloat(),value.toFloat()))
//            chart?.notifyDataSetChanged();
//            chart?.invalidate();
//        }
        return values
    }

    //차트 그리기
    private fun setChart(label: String, duration: Int) {
        var values = ArrayList<BarEntry>()
        //var value = 0
        chart = binding?.chart
        when(label){
            "수면 효율" ->{
                values = changeChartData(scoreList!!,duration)
            }
            "침대에서 보낸 시간"->{
                values = changeChartData(bedTimeList!!,duration)
            }
            "수면 시간"->{
                values = changeChartData(sleepTimeList!!,duration)
            }
        }

        val set1 = BarDataSet(values, label)

        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(set1)

        val data: BarData = BarData(dataSets)

        chart?.legend?.textColor = Color.parseColor("#FCA311")
        chart?.xAxis?.textColor = Color.parseColor("#E5E5E5")
        chart?.axisLeft?.textColor = Color.parseColor("#E5E5E5")
        chart?.axisRight?.textColor = Color.parseColor("#14213D")
        chart?.animateY(500)
        chart?.description?.isEnabled = false
        chart?.setDrawGridBackground(false)
        chart?.legend?.textSize = 14f


        set1.setColor(Color.parseColor("#FCA311"))
//        set1.setCircleColor(Color.parseColor("#FCA311"))
//        set1.setDrawCircles(false)
        set1.setDrawValues(false)

        chart?.setData(data)
    }

//    private fun showChart(label: String) {
//        setChart(label)
//    }


    //수면효율
    private fun calScore(
        bedStartTime: String,
        bedEndTime: String,
        startSleepTime: String,
        wakeUpTime: String
    ): Int {
        val score =
            (calSleepTime(startSleepTime, wakeUpTime).toDouble() / calBedTime(
                bedStartTime,
                bedEndTime
            ).toDouble()) * 100.0
        return score.toInt()
    }

    //침상시간
    private fun calBedTime(bedStartTime: String, bedEndTime: String): Int {
        var bedTime = 0
        if (calTimeInMins(bedStartTime) > dayInMins) {
            bedTime = calTimeInMins(bedEndTime) - calTimeInMins(bedStartTime)
        } else {
            bedTime = dayInMins - calTimeInMins(bedStartTime) + calTimeInMins(bedEndTime)
        }
        return bedTime
    }

    //잠들기까지 든 시간
    private fun calBI(bedStartTime: String, startSleepTime: String): Int {
        if (calTimeInMins(bedStartTime) > dayInMins) {
            return calTimeInMins(startSleepTime) - calTimeInMins(bedStartTime)
        } else if (calTimeInMins(startSleepTime) > dayInMins) {
            return dayInMins - calTimeInMins(bedStartTime) + calTimeInMins(startSleepTime)
        } else {
            return calTimeInMins(startSleepTime) - calTimeInMins(bedStartTime)
        }
    }

    //수면 시간
    private fun calSleepTime(startSleepTime: String, wakeUpTime: String): Int {
        if (calTimeInMins(startSleepTime) < dayInMins) {
            return dayInMins - calTimeInMins(startSleepTime) + calTimeInMins(wakeUpTime)
        } else {
            return calTimeInMins(wakeUpTime) - calTimeInMins(startSleepTime)
        }
    }

    //오늘 날짜 가져오기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val current = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formatted = current.format(formatter)

        return formatted
    }

    //분단위 시간으로 바꾸기
    private fun calTimeInMins(time: String): Int {
        val time = time.removeSurrounding("\"")
        val arr = time.split(":")
        val hour = arr[0].toInt()
        val minute = arr[1].toInt()

        return hour * 60 + minute
    }

    //분단위 시간을 "hh:mm" 으로 바꾸기
    private fun changeTimeFormat(time: Int): String {
        val df = DecimalFormat("00")
        val hour = df.format(time / 60).toString()
        val minute = df.format(time % 60).toString()

        return "$hour:$minute"
    }

    //분단위 시간을 "hh시간 mm분"으로 바꾸기
    private fun changeTimeFormat2(time: Int): String {
        val df = DecimalFormat("00")
        val hour = df.format(time / 60).toString()
        val minute = df.format(time % 60).toString()

        return "$hour" + "시간 " + "$minute" + "분"
    }

    private fun changeDateFormat(date:String):String{
        var date = date
        val day = date.drop(6)
        date = date.drop(4)
        val month = date.dropLast(2)

        return "$month/$day"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDaysAgo(day: Long): String {
        val dayAgo = LocalDate.now().minusDays(day)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formatted = dayAgo.format(formatter)

        return formatted
    }

    //firestore에서 데이터가져와서 로컬 db에 저장
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getMonthlyDbDataList(key: String): ArrayList<String> {
        val dbDataList = ArrayList<String>()
        for (i in 0..29) {
            dbDataList?.add(i.toString())
        }
        for (i in 0..29) {
            var date = getDaysAgo(i.toLong())
            db.collection("Data").document("catree42@gmail.com").collection("sleepdata")
                .document(date)
                .get()
                .addOnSuccessListener { result ->
                    var data = result[key]
                    if (data != null)
                        dbDataList?.set(i, data as String)
                    else {
                        dbDataList?.set(i, "null")
                    }
                    //
                    // Log.d("dbDataList", dbDataList?.get(i)!!)
                }.await()
        }
        return dbDataList!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun calBedTimeAverage() {
        bedTimeList = ArrayList<com.example.sleephelper.ChartData>()
        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (bedEndTimeList?.get(i) != "null" &&
                bedStartTimeList?.get(i) != "null"
            ) {
                count++
                sum += calBedTime(bedStartTimeList?.get(i)!!, bedEndTimeList?.get(i)!!)
            }
        }
        val weeklyAverage = sum / count
        val weeklyAverageFormat = changeTimeFormat2(weeklyAverage)

        sum = 0
        count = 0
        var bedTime = 0
        var date :String? = null
        for (i in 0..29) {
            if (bedEndTimeList?.get(i) != "null" &&
                bedStartTimeList?.get(i) != "null"
            ) {
                count++
                bedTime = calBedTime(
                    bedStartTimeList?.get(i)!!,
                    bedEndTimeList?.get(i)!!
                )
                date = getDaysAgo(i.toLong())
                bedTimeList?.add(com.example.sleephelper.ChartData(bedTime,date))
                sum += bedTime
            }
            else{
                bedTimeList?.add(com.example.sleephelper.ChartData(0,""))
            }
        }
        val monthlyAverage = sum / count
        val monthlyAverageFormat = changeTimeFormat2(monthlyAverage)
        reportDataList?.set(1, ReportData(weeklyAverageFormat, monthlyAverageFormat, false))


    }

    private suspend fun calSleepTimeAverage() {
        sleepTimeList = ArrayList<ChartData>()
        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (startSleepTimeList?.get(i) != "null" && wakeUpTimeList?.get(i) != "null") {
                count++
                sum += calSleepTime(startSleepTimeList!!.get(i), wakeUpTimeList!!.get(i))
            }
        }
        val weeklyAverage = sum / count
        val weeklyAverageFormat = changeTimeFormat2(weeklyAverage)

        sum = 0
        count = 0
        var sleepTime = 0
        var date : String? = null
        for (i in 0..29) {
            if (startSleepTimeList?.get(i) != "null" && wakeUpTimeList?.get(i) != "null") {
                count++
                sleepTime = calSleepTime(startSleepTimeList!!.get(i), wakeUpTimeList!!.get(i))
               date = getDaysAgo(i.toLong())
                sleepTimeList?.add(com.example.sleephelper.ChartData(sleepTime,date))
                sum += sleepTime
            }else{
                sleepTimeList?.add(com.example.sleephelper.ChartData(0,""))
            }
        }
        val monthlyAverage = sum / count
        val monthlyAverageFormat = changeTimeFormat2(monthlyAverage)

        reportDataList?.set(2, ReportData(weeklyAverageFormat, monthlyAverageFormat, false))
    }

    private suspend fun calBIAverage() {

        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (bedStartTimeList?.get(i) != "null" && startSleepTimeList?.get(i) != "null") {
                count++
                sum += calBI(bedStartTimeList!!.get(i), startSleepTimeList!!.get(i))
            }
        }
        //Log.d("Bi Sum",sum.toString())
        val weeklyAverage = (sum / count).toString()

        sum = 0
        count = 0

        for (i in 0..29) {
            if (bedStartTimeList?.get(i) != "null" && startSleepTimeList?.get(i) != "null") {
                count++
                sum += calBI(bedStartTimeList!!.get(i), startSleepTimeList!!.get(i))
            }
        }
        val monthlyAverage = (sum / count).toString()

        reportDataList?.set(3, ReportData(weeklyAverage, monthlyAverage, false))

    }

    private fun calScoreAverage() {
        scoreList = kotlin.collections.ArrayList<com.example.sleephelper.ChartData>()
        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (bedStartTimeList?.get(i) != "null" && bedEndTimeList?.get(i) != "null"
                && startSleepTimeList?.get(i) != "null" && wakeUpTimeList?.get(i) != "null"
            ) {
                count++
                sum += calScore(
                    bedStartTimeList!!.get(i), bedEndTimeList!!.get(i),
                    startSleepTimeList!!.get(i), wakeUpTimeList!!.get(i)
                )
            }

        }
        val weeklyAverage = (sum / count).toString()

        sum = 0
        count = 0
        var score = 0
        var date :String? = null
        for (i in 0..29) {
            if (bedStartTimeList?.get(i) != "null" && bedEndTimeList?.get(i) != "null"
                && startSleepTimeList?.get(i) != "null" && wakeUpTimeList?.get(i) != "null"
            ) {
                count++
                date = getDaysAgo(i.toLong())
                score = calScore(bedStartTimeList!!.get(i), bedEndTimeList!!.get(i),
                    startSleepTimeList!!.get(i), wakeUpTimeList!!.get(i))
                scoreList?.add(com.example.sleephelper.ChartData(score,date))
                sum += score
            }else{
                scoreList?.add(com.example.sleephelper.ChartData(0,""))
            }

        }

        val monthlyAverage = (sum / count).toString()

        reportDataList?.set(0, ReportData(weeklyAverage, monthlyAverage, false))
    }

    private fun calStartSleepTimeAverage() {
        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (startSleepTimeList?.get(i) != "null") {
                count++
                sum += calTimeInMins(startSleepTimeList!!.get(i))
            }
        }
        val weeklyAverage = changeTimeFormat(sum / count)

        sum = 0
        count = 0

        for (i in 0..29) {
            if (startSleepTimeList?.get(i) != "null") {
                count++
                sum += calTimeInMins(startSleepTimeList!!.get(i))
            }
        }
        val monthlyAverage = changeTimeFormat(sum / count)

        reportDataList?.set(4, ReportData(weeklyAverage, monthlyAverage, false))
    }

    private fun calWakeUpTimeAverage() {
        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (wakeUpTimeList?.get(i) != "null") {
                count++
                sum += calTimeInMins(wakeUpTimeList!!.get(i))
            }
        }
        val weeklyAverage = changeTimeFormat(sum / count)

        sum = 0
        count = 0

        for (i in 0..29) {
            if (wakeUpTimeList?.get(i) != "null") {
                count++
                sum += calTimeInMins(wakeUpTimeList!!.get(i))
            }
        }
        val monthlyAverage = changeTimeFormat(sum / count)

        reportDataList?.set(5, ReportData(weeklyAverage, monthlyAverage, false))
    }

    private fun calNapTimeAverage() {
        var sum = 0
        var count = 0

        for (i in 0..6) {
            if (napTimeList?.get(i) != "null") {
                count++
                sum += calTimeInMins(napTimeList!!.get(i))
            }
        }
        val weeklyAverage = changeTimeFormat(sum / count)

        sum = 0
        count = 0

        for (i in 0..29) {
            if (napTimeList?.get(i) != "null") {
                count++
                sum += calTimeInMins(napTimeList!!.get(i))
            }
        }
        val monthlyAverage = changeTimeFormat(sum / count)

        reportDataList?.set(6, ReportData(weeklyAverage, monthlyAverage, false))
    }

    private fun calCoffeeAverage() {
        var sum = 0.0
        var count = 0.0
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.DOWN

        for (i in 0..6) {
            if (coffeeList?.get(i) != "null") {
                count++
                sum += coffeeList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val weeklyAverage = df.format(sum / count)

        sum = 0.0
        count = 0.0

        for (i in 0..29) {
            if (coffeeList?.get(i) != "null") {
                count++
                sum += coffeeList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val monthlyAverage = df.format(sum / count)

        reportDataList?.set(7, ReportData(weeklyAverage + "잔", monthlyAverage + "잔", false))
    }

    private fun calBeerAverage() {
        var sum = 0.0
        var count = 0.0
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.DOWN

        for (i in 0..6) {
            if (beerList?.get(i) != "null") {
                count++
                sum += beerList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val weeklyAverage = df.format(sum / count)

        sum = 0.0
        count = 0.0

        for (i in 0..29) {
            if (beerList?.get(i) != "null") {
                count++
                sum += beerList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val monthlyAverage = df.format(sum / count)

        reportDataList?.set(8, ReportData(weeklyAverage + "잔", monthlyAverage + "잔", false))

    }

    private fun calSojuAverage() {
        var sum = 0.0
        var count = 0.0
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.DOWN

        for (i in 0..6) {
            if (sojuList?.get(i) != "null") {
                count++
                sum += sojuList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val weeklyAverage = df.format(sum / count)

        sum = 0.0
        count = 0.0

        for (i in 0..29) {
            if (sojuList?.get(i) != "null") {
                count++
                sum += sojuList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val monthlyAverage = df.format(sum / count)

        reportDataList?.set(9, ReportData(weeklyAverage + "잔", monthlyAverage + "잔", false))

    }

    private fun calMakgeolliAverage() {
        var sum = 0.0
        var count = 0.0
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.DOWN

        for (i in 0..6) {
            if (makgeolliList?.get(i) != "null") {
                count++
                sum += makgeolliList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val weeklyAverage = df.format(sum / count)

        sum = 0.0
        count = 0.0

        for (i in 0..29) {
            if (makgeolliList?.get(i) != "null") {
                count++
                sum += makgeolliList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val monthlyAverage = df.format(sum / count)

        reportDataList?.set(10, ReportData(weeklyAverage + "잔", monthlyAverage + "잔", false))

    }

    private fun calWineAverage() {
        var sum = 0.0
        var count = 0.0
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.DOWN

        for (i in 0..6) {
            if (wineList?.get(i) != "null") {
                count++
                sum += wineList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val weeklyAverage = df.format(sum / count)

        sum = 0.0
        count = 0.0

        for (i in 0..29) {
            if (wineList?.get(i) != "null") {
                count++
                sum += wineList!!.get(i).removeSurrounding("\"").toDouble()
            }
        }
        val monthlyAverage = df.format(sum / count)

        reportDataList?.set(11, ReportData(weeklyAverage + "잔", monthlyAverage + "잔", false))

    }

    private fun setUpWeeklyView() {
        duration = Duration.WEEK.duration
        binding?.btnWeekly?.background =
            getDrawable(R.drawable.report_selected_left_button_background)
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

    private fun setUpMonthlyView() {
        duration = Duration.MONTH.duration
        binding?.btnWeekly?.background = getDrawable(R.drawable.report_left_button_background)
        binding?.btnMonthly?.background =
            getDrawable(R.drawable.report_selected_right_button_background)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_weekly -> {
                setUpWeeklyView()
            }
            R.id.btn_monthly -> {
                setUpMonthlyView()
            }
            R.id.clScore -> {
                setChart("수면 효율",duration!!)
                binding?.chart?.visibility = View.VISIBLE
                v!!.background = ContextCompat.getDrawable(this,R.drawable.report_selected_background)
                binding?.clTimeInBed?.background = null
                binding?.clSleepTime?.background = null
                binding?.tvScore?.setTextColor(Color.WHITE)
                binding?.tvTimeInBed?.setTextColor(ContextCompat.getColor(this,R.color.orange))
                binding?.tvSleepTime?.setTextColor(ContextCompat.getColor(this,R.color.orange))

            }
            R.id.clTimeInBed -> {
                setChart("침대에서 보낸 시간",duration!!)
                binding?.chart?.visibility = View.VISIBLE
                v!!.background = ContextCompat.getDrawable(this,R.drawable.report_selected_background)
                binding?.clScore?.background = null
                binding?.clSleepTime?.background = null
                binding?.tvTimeInBed?.setTextColor(Color.WHITE)
                binding?.tvScore?.setTextColor(ContextCompat.getColor(this,R.color.orange))
                binding?.tvSleepTime?.setTextColor(ContextCompat.getColor(this,R.color.orange))

            }
            R.id.clSleepTime -> {
                setChart("수면 시간",duration!!)
                binding?.chart?.visibility = View.VISIBLE
                v!!.background = ContextCompat.getDrawable(this,R.drawable.report_selected_background)
                binding?.clTimeInBed?.background = null
                binding?.clScore?.background = null
                binding?.tvSleepTime?.setTextColor(Color.WHITE)
                binding?.tvScore?.setTextColor(ContextCompat.getColor(this,R.color.orange))
                binding?.tvTimeInBed?.setTextColor(ContextCompat.getColor(this,R.color.orange))


            }
        }
    }

    private fun showProgress(isShow: Boolean) {
        if (isShow) binding?.llProgress?.visibility = View.VISIBLE
        else binding?.llProgress?.visibility = View.GONE
    }

    private fun init() {
        binding?.cdlParent?.visibility = View.GONE
        showProgress(true)

        CoroutineScope(Dispatchers.Default).launch {

            bedEndTimeList = getMonthlyDbDataList("bedEndTime")
            bedStartTimeList = getMonthlyDbDataList("bedStartTime")
            beerList = getMonthlyDbDataList("beer")
            coffeeList = getMonthlyDbDataList("coffee")
            emojiList = getMonthlyDbDataList("emoji")
            makgeolliList = getMonthlyDbDataList("makgeolli")
            napTimeList = getMonthlyDbDataList("napTime")
            sojuList = getMonthlyDbDataList("soju")
            startSleepTimeList = getMonthlyDbDataList("startSleepTime")
            wakeUpCountList = getMonthlyDbDataList("wakeUpCount")
            wakeUpTimeList = getMonthlyDbDataList("wakeUpTime")
            wineList = getMonthlyDbDataList("wine")

            calScoreAverage()
            calBedTimeAverage()
            calSleepTimeAverage()
            calBIAverage()
            calStartSleepTimeAverage()
            calWakeUpTimeAverage()
            calNapTimeAverage()
            calCoffeeAverage()
            calBeerAverage()
            calSojuAverage()
            calMakgeolliAverage()
            calWineAverage()



            runOnUiThread {
                showProgress(false)
                Toast.makeText(
                    this@ReportActivity,
                    "데이터 로딩을 완료했습니다",
                    Toast.LENGTH_SHORT
                ).show()
                setUpWeeklyView()
                binding?.cdlParent?.visibility = View.VISIBLE

            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
}
