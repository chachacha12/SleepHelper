package com.example.sleephelper


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.example.sleephelper.databinding.ActivityRecommendTimeBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecommendTimeActivity : AppCompatActivity() {

    private var input: String = ""
    private var binding: ActivityRecommendTimeBinding? = null

    var range1 : String = ""
    var range2 : String = ""

    var auth : FirebaseAuth?= null
    var googleSignInClient : GoogleSignInClient?= null
    var firestore : FirebaseFirestore? = null
    val db = FirebaseFirestore.getInstance()
    // firebase에서 받아온 정보를 저장할 변수 선언

    var wine:String="null" // 와인 섭취량00
    var beer:String="null"// 맥주 섭취량
    var makgeolli:String="null" // 탁주 섭취량
    var soju:String="null"// 소주 섭취량
    var coffee:String="null" // 커피 섭취량
    var nap_time:String = "12:00"// 낮잠 시간
    var sleep_time:String= "12:00"// 취침 시각
    var wakeup_time:String= "12:00" // 기상 시각
    var gotobed_time:String= "12:00" // 침대에 누운 시각
    var outtobed_time:String= "12:00"// 침대에서 벗어난 시각
    lateinit var filePath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendTimeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth!!.currentUser?.email
        getData()
        setBottomNavigation()
        connectToAI()


        binding!!.userName.text = auth?.currentUser?.displayName!!
        binding!!.dateTextView.text = LocalDate.now().toString()
    }

    private fun getData() {

        //filePath = "20221124"
        var now = LocalDate.now()
        var value:String = ""

        var date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        Log.e("date", date)

        filePath = date
        Log.e("log",filePath)

        firestore!!.collection("Data")
            .document(auth?.currentUser?.email!!)
            .collection("sleepdata").document(
                filePath).get()      // 문서 가져오기
            .addOnSuccessListener { result ->
                // 성공할 경우
                Log.e("log",filePath)
                Log.e("로그", result.get("beer").toString())


                // 수면일기에 입력된 정보 변수에 넣기
                wine = result.get("wine").toString()
                beer = result.get("beer").toString()
                makgeolli = result.get("makgeolli").toString()
                soju = result.get("soju").toString()
                coffee = result.get("coffee").toString()
                nap_time = result.get("napTime").toString()
                sleep_time = result.get("startSleepTime").toString()
                wakeup_time = result.get("wakeUpTime").toString()
                gotobed_time = result.get("bedStartTime").toString()
                outtobed_time = result.get("bedEndTime").toString()


                //1.(잠자리에서 벗어난 시각- 잠자리에 들어간 시각) – (기상 시각- 취침 시각)
                Log.e("logout",outtobed_time)
                Log.e("loggo",gotobed_time)
                val value1temp1 = calTime(gotobed_time,outtobed_time)
                val value1temp2 = calTime(sleep_time,wakeup_time)
                val value1 = value1temp1 - value1temp2


                //2.	취침 시각 – 잠자리에 들어간 시각
                val value2 = calTime(gotobed_time,sleep_time)

                // 3.	잠자리에서 벗어난 시각 – 기상 시각
                val value3 = calTime(wakeup_time,outtobed_time)
                Log.e("loggo",value3.toString())

                // 4.	30 (눈감고 잠드는 시각인데 입력 안 받아서 그냥 30으로 하겠습니다.)
                val value4 = 30
                //5.	수면제 (이건 입력 안 받는 것으로 아는데 안 받으면 0으로 하겠습니다.)
                val value5  = 0
                //6.	커피
                val value6 = coffee
                // 7.	알코올 (아래 사진보고 점수 계산해서 넘겨야 할 것 같습니다.)
                var score = beer.toInt() + (soju.toInt())*0.5 + makgeolli.toInt() + (wine.toInt())*0.5
                if(score > 5){
                    score = 5.0
                }
                    // 소숫점 버림
                val value7 = score.toInt()

                //8.	0 (낮잠인데 이것도 그냥 0으로 하겠습니다.)
                var value8 = 0

                value = value1.toString()+"/"+value2.toString()+"/"+value3.toString()+"/"+
                        value4.toString()+"/"+value5.toString()+"/"+value6+"/"+value7.toString()+"/"+value8.toString()

                Log.e("value",value)

                input = value




            }
            .addOnFailureListener { exception ->
                // 실패할 경우
                Log.w("MainActivity", "Error getting documents: $exception")
            }



    }

    var wakeUpTime: String = ""

    private fun connectToAI() {



        val timePicker: TimePicker = findViewById(R.id.timepicker)

        timePicker.setOnTimeChangedListener { timePicker, hourOfday, minute
            -> binding!!.BtntoAIServer.text = "${hourOfday}시 ${minute}분 설정"
               wakeUpTime = "${hourOfday}:${minute}"
            Log.e("wake",wakeUpTime)
        }

        binding!!.BtntoAIServer.setOnClickListener {


            // AI 서버로 소켓 네트워크
            ClientThread().start()

        }
    }

    inner class ClientThread: Thread(){
        override fun run() {
            try {

//              소켓 생성
                val socket = Socket("192.168.163.59", 80)

//              서버에 보낼 값 쓰는 것. println에 값 넣으면 된다.
                val printWriter= PrintWriter(socket.getOutputStream())
                printWriter.println(input)
                printWriter.flush()

//              서버에서 보낸 값 읽어오는 것
                val reader= InputStreamReader(socket.getInputStream())
                val bufReader= BufferedReader(reader)
                val buf=StringBuffer()

                var str:String?=null

                while(bufReader.readLine().also{str= it}!=null){
                    buf.append("""$str""")
                }

                runOnUiThread {
//              여기서 buf.toString이 읽어온 값이다.
                    Log.e("koggg",buf.toString())

                    range1 =buf.toString().split(" ")[0]
                    range2 =buf.toString().split(" ")[1]


//                    Log.e("minToTimeNum",minToTimeNum("500"))
//                    Log.e("minToTimeNum",minToTimeNum("550"))

                    range1 = calTime(minToTimeNum(range1),wakeUpTime).toString()
                    range2 = calTime(minToTimeNum(range2),wakeUpTime).toString()

//                    Log.e("range1",range1)
//                    Log.e("range2",range2)

                    binding!!.RecommendTimetextView.text=minToTime2(range2)+"~"+minToTime2(range1)
                    Log.e("inptt",input)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

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
    private fun minToTime(test: String) : String {
        var input = test.toInt()
        var hour = input/60
        var min = input - hour * 60

        var result : String = hour.toString() + "시간" + min.toString() + "분"

        return result
    }

    private fun minToTime2(test: String) : String {
        var input = test.toInt()
        var hour = input/60
        var min = input - hour * 60

        var result : String = hour.toString() + ":" + min.toString()

        return result
    }

    private fun minToTimeNum(test: String) : String {
        var input = test.toInt()
        var hour = input/60
        var min = input - hour * 60

        var result : String = hour.toString() + ":" + min.toString()

        return result
    }

    private fun calTime(gotobedtime: String, outtobedtime: String): Int {
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



    // 수면일기 (+) 버튼
    private fun setFabAdd(){
        binding!!.fabAdd.setOnClickListener(){
            intent = Intent(this, WritingDiaryActivity::class.java)
            startActivity(intent)
        }
    }



    private fun setBottomNavigation() {
        binding!!.bottomNavigation.setOnItemSelectedListener(){
            when(it.itemId){
                R.id.nav_calendar -> {
                    intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_report -> {
                    intent = Intent(this, ReportActivity::class.java)
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