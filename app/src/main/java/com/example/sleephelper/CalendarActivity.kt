package com.example.sleephelper

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.sleephelper.databinding.ActivityCalendarBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class CalendarActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCalendarBinding
    private lateinit var chart : LineChart

    var firebaseAuth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    // firebase에서 받아온 정보를 저장할 변수 선언
    var wine:String?="1" // 와인 섭취량
    var beer:String?="1" // 맥주 섭취량
    var makguli:String?="1" // 탁주 섭취량
    var soju:String?="1" // 소주 섭취량
    var coffee:String?="1" // 커피 섭취량
    var nap_time:String = "1:00"// 낮잠 시간
    var sleep_time:String= "23:30"// 취침 시각
    var wakeup_time:String= "09:00" // 기상 시각
    var gotobed_time:String= "23:00" // 침대에 누운 시각
    var outtobed_time:String= "10:00"// 침대에서 벗어난 시각
    var emoji:String= "1" // 수면 이모지

    private var reportDataList: ArrayList<ReportData>? = null
    private var db = FirebaseFirestore.getInstance()

    //각 항목 들의 데이터를 30일 치 담는 리스트
    private var dbDataList: QuerySnapshot? = null
    private var emojiList: kotlin.collections.ArrayList<SleepData>? = null

    var time_to_sleep:String?=null // 잠들 때까지 걸린 시간
    var time_in_bed:String?=null // 침대에서 머문 시간
    var sleeptime:String?=null // 총 수면 시간

    lateinit var time1:String
    lateinit var time2: String
    lateinit var time3:String
    lateinit var time4:String
    lateinit var time5:String
    lateinit var time6:String
    lateinit var sleepLevel:String
    lateinit var filePath:String

    var emojiPath:Int = 0 // 사용자가 선택한 이모지를 캘린더에 나타내기 한 변수

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // firebase 연동
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth!!.currentUser?.email

        reportDataList = ReportModel.defaultReportDataList()

        init()
        initDate()
        setChart()

        setBottomNavigation()
        setFabAdd()
    }

    // 앱 시작 시 수면 일기 작성된 날 그에 해당하는 이모지를 보여주는 메소드
    private fun init(){
        CoroutineScope(Dispatchers.Default).launch{
            val calenderView:CalendarView = binding.calendarView as CalendarView

            getDbDataList()
            emojiList = getDataList("emoji")
            Log.e("emojilist", emojiList.toString())

            val events: MutableList<EventDay> = ArrayList()
            var calendar:Calendar = Calendar.getInstance()

            var arrayLen = emojiList!!.size
            //Log.e("arrayLen", arrayLen.toString())

            for(i: Int in 0..arrayLen-1){
                var ddate = emojiList?.get(i)?.date.toString()
                var dyear = ddate.substring(0 until 4)
                var dmonth = ddate.substring(4 until 6)
                var dday = ddate.substring(6 until 8)

                var setDate = dateToCalendar(dyear.toInt(), dmonth.toInt()-1, dday.toInt())
                calendar = setDate

                if(emojiList?.get(i)?.value.toString() == "1"){
                    emojiPath = R.drawable.emoji_bad
                }
                else if(emojiList?.get(i)?.value.toString() == "2"){
                    emojiPath = R.drawable.emoji_normal
                }
                else if(emojiList?.get(i)?.value.toString() == "3"){
                    emojiPath = R.drawable.emoji_good
                }
                //Log.e("pair", calendar.toString())
                //Log.e("pair2", emojiPath.toString())

                //binding.slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED

                events.add(EventDay(calendar, emojiPath))

                runOnUiThread(Runnable(){
                    calenderView.setEvents(events)
                })

            }

            /*
            var setDate = dateToCalendar(2022,11,28)
            calendar = setDate

            events.add(EventDay(calendar, R.drawable.emoji_bad))
             */
        }
    }

    // Local date 형식 -> Calendar 형식으로 변환
    private fun dateToCalendar(year:Int, month:Int, date:Int):Calendar{

        var c : Calendar = Calendar.getInstance()
        c.set(year, month, date)
        return c

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
                }.await()
        }
        return dbDataList!!
    }

    private suspend fun getDbDataList() {
        // var dbDataList : QuerySnapshot? = null
        while (true) {
            db.collection("Data").document(firebaseAuth?.currentUser!!.email!!).collection("sleepdata")
                .get()
                .addOnSuccessListener { result ->
                    dbDataList = result
                }.await()
            if (dbDataList != null) {
                break
            }
        }
    }

    private fun getDataList(key: String): kotlin.collections.ArrayList<SleepData> {
        var dataList = kotlin.collections.ArrayList<SleepData>()
        var date: String? = null
        var value: String? = null
        for (dbData in dbDataList!!) {
            if (dbData.id.toInt() >= getDaysAgo(30.toLong()).toInt()) {
                date = dbData.id
                value = dbData.data[key] as String
                dataList.add(SleepData(date, value))
            }
        }
        return dataList
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initDate(){

        binding.calendarView.setOnDayClickListener(OnDayClickListener { eventDay ->

            val calendarView: CalendarView = binding.calendarView as CalendarView
            val events: MutableList<EventDay> = ArrayList()


            val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
            val date = simpleDateFormat.format(eventDay.calendar.time)

            filePath = date.toString()
            Log.e("date", date)
            Log.e("filePath", filePath)

            firestore!!.collection("Data")
                .document(firebaseAuth?.currentUser?.email!!)
                .collection("sleepdata").document(filePath).get()      // 문서 가져오기
                .addOnSuccessListener { result ->
                    // 성공할 경우
                    Log.e("after access filepath",filePath)
                    Log.e("clickedday beer", result.get("beer").toString())

                    val clickedDayCalendar : Calendar = eventDay.calendar

                    // 수면일기에 입력된 정보 변수에 넣기
                    wine = result.get("wine").toString() + "잔"
                    beer = result.get("beer").toString() + "잔"
                    makguli = result.get("makgeolli").toString() + "잔"
                    soju = result.get("soju").toString() + "잔"
                    coffee = result.get("coffee").toString() + "잔"
                    nap_time = result.get("napTime").toString()
                    sleep_time = result.get("startSleepTime").toString()
                    wakeup_time = result.get("wakeUpTime").toString()
                    gotobed_time = result.get("bedStartTime").toString()
                    outtobed_time = result.get("bedEndTime").toString()
                    emoji = result.get("emoji").toString()

                    time1 = sleep_time.toString()
                    time2 = gotobed_time.toString()
                    calTimeToSleep(time2, time1)

                    time3 = outtobed_time.toString()
                    time4 = calTimeinBed(time2, time3).toString()
                    time_in_bed = minToTime(time4!!)

                    time5 = wakeup_time.toString()
                    time6 = calTimeinBed(time1, time5).toString()
                    sleeptime = minToTime(time6)

                    sleepLevel = emoji.toString()


                    // 사용자가 선택한 수면 이모지에 따라 다른 경로 설정
                    if(sleepLevel!!.toInt()==1){
                        emojiPath = R.drawable.emoji_bad
                    }
                    else if(sleepLevel!!.toInt()==2){
                        emojiPath = R.drawable.emoji_normal
                    }
                    else if(sleepLevel!!.toInt()==3){
                        emojiPath = R.drawable.emoji_good
                    }

                    binding.tvWine.text = wine
                    binding.tvBeer.text = beer
                    binding.tvMakgurli.text = makguli
                    binding.tvSoju.text = soju
                    binding.tvCoffee.text = coffee
                    binding.tvNapTime.text = nap_time
                    binding.tvBedTime.text = sleep_time
                    binding.tvWakeUpTime.text = wakeup_time
                    binding.tvBI.text = time_to_sleep
                    binding.tvTimeInBed.text = time_in_bed
                    binding.tvSleepTime.text = sleeptime
                    binding.tvScore.text = calScore().toString()
                    binding.slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED

                    events.add(EventDay(clickedDayCalendar, emojiPath))
                    calendarView.setEvents(events)

                    init()

                }
                .addOnFailureListener { exception ->
                    // 실패할 경우
                    //Toast.makeText(this@CalendarActivity, "해당 날짜에 작성된 \n수면 일기가 없습니다.", Toast.LENGTH_SHORT)
                    onStart()
                    Log.w("MainActivity", "Error getting documents: $exception")
                }

        })


    }

    // 잠들 때까지 걸린 시간 계산 메소드
    private fun calTimeToSleep(test: String, test2:String){
        var cal: Int = timeToMin(test2)-timeToMin(test)

        if(cal<0){
            cal = cal + 1440
        }

        time_to_sleep = cal.toString()
    }

    // 시간 -> 분
    private fun timeToMin(test: String) : Int {
        //var input = test
        var token = test.split(":")
        var h: Int = token[0].toInt()
        var m: Int = token[1].toInt()
        var result: Int = 60 * h + m

        return result
    }

    private fun isTrue(test: String) : Int{
        var token = test.split(":")
        var hour : Int = token[0].toInt()

        return hour
    }

    // 침대에서 머문 시간 계산 메소드
    private fun calTimeinBed(gotobedtime: String, outtobedtime: String): Int {
        var timeinbed = 0

        if (isTrue(outtobedtime) - isTrue(gotobedtime) < 0){
            timeinbed = 1440 - timeToMin(gotobedtime) + timeToMin(outtobedtime)
        }
        else {
            //timeinbed = 1440 - timeToMin(gotobedtime) + timeToMin(outtobedtime)
            timeinbed = timeToMin(outtobedtime) - timeToMin(gotobedtime)
        }

        if(timeinbed < 0){
            timeinbed = 1440 - (-1*timeinbed)
        }

        return timeinbed
    }

    // 분 -> 시
    private fun minToTime(test: String) : String {
        var input = test.toInt()
        var hour = input/60
        var min = input - hour * 60

        var result : String = hour.toString() + "시간" + min.toString() + "분"

        return result
    }

    private fun setChart(){
        chart = binding.sleepLineChart


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

    //수면효율 계산
    private fun calScore(): Int {
        val score =
            (calTimeinBed(sleep_time.toString(), wakeup_time.toString()).toDouble() / calTimeinBed(
                gotobed_time.toString(), outtobed_time.toString()
            ).toDouble()) * 100.0
        return score.toInt()
    }

    private fun setFabAdd(){
        binding!!.fabAdd.setOnClickListener(){
            intent = Intent(this, WritingDiaryActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBottomNavigation(){
        binding!!.bottomNavigation.setOnItemSelectedListener(){
            when(it.itemId){
                R.id.nav_report -> {
                    intent = Intent(this, ReportActivity::class.java)
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

}