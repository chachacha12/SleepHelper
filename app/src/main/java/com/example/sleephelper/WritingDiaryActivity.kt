package com.example.sleephelper

// import com.google.firebase.auth.ktx.auth
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sleephelper.databinding.ActivityWritingDiaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WritingDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWritingDiaryBinding
    var firebaseAuth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    var showDate:String?=null //화면에 띄워줄 날짜 포멧 저장 변수
    var sendDate:String?=null //DB에 보내줄 날짜 포멧 저장 변수

    //수면일기 입력값 13개
    var emoji=3
    var gotobed_time =""
    var sleep_time =""
    var sleep_peroid =""
    var wakeup_time =""
    var outtobedtime =""
    var wakeup_num =0
    var daysleep_peroid =""
    var coffee =0
    var beer =0
    var soju =0
    var makguli =0
    var wine =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        Log.e("태그","현재 로그인한 유저 이메일:"+ firebaseAuth?.currentUser?.email)

        //날짜값들 세팅
        setDate()
        //태그값 선택시 처리
        ChooseTagEvent()

        //이모지 선택할때 동작할 이벤트 처리
        emoji_event()

        //완료버튼 클릭시
        binding.checkButton.setOnClickListener{
            //로그인된 유저만 데이터 저장 가능
            if(firebaseAuth?.currentUser?.email==null){
                Toast.makeText(this, "로그인 해주세요",Toast.LENGTH_SHORT).show()
            }else{
                if(!binding.gotobedtimeEdittextView.text.isEmpty() && !binding.sleepclockEdittextView.text.isEmpty() && !binding.sleeptimeEdittextView.text.isEmpty()
                    && !binding.waketimeEdittextView.text.isEmpty() && !binding.outtobedtimeEdittextView.text.isEmpty() ){
                    Writediary() ////태그제외 입력한 수면일기값들 저장
                    saveSleepData()
                }else{
                    Toast.makeText(this, "필수입력정보를 모두 입력해주세요",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //이모지 선택할때 동작할 이벤트 처리
    fun emoji_event(){
        binding.emojiTextView.setOnClickListener {
            //다이얼로그 객체 생성
            val dlg = EmojiDialog(this)
            //이모지 선택 이벤트처리
            dlg.setOnEmojiClickedListener {
                binding.imageView.setImageResource(it)   //선택한 이모지 set해줌
                //선택한 이모지에 따라 emoji변수에 다른 값 저장
                if(it == R.drawable.emoji_good){
                    emoji =3
                }else if(it == R.drawable.emoji_normal){
                    emoji =2
                }else{
                    emoji =1
                }
                binding.imageView.visibility = View.VISIBLE  //이모자 보여줌
                binding.emojiTextView.visibility = View.GONE
            }
            dlg.show()
        }
        //이모지클릭시
        binding.imageView.setOnClickListener {
            //다이얼로그 객체 생성
            val dlg = EmojiDialog(this)
            //이모지 선택 이벤트처리
            dlg.setOnEmojiClickedListener {
                binding.imageView.setImageResource(it)   //선택한 이모지 set해줌
                //선택한 이모지에 따라 emoji변수에 다른 값 저장
                if(it == R.drawable.emoji_good){
                    emoji =3
                }else if(it == R.drawable.emoji_normal){
                    emoji =2
                }else{
                    emoji =1
                }
            }
            dlg.show()
        }
    }

    //날짜값들 세팅
    @SuppressLint("SimpleDateFormat")
    fun setDate(){
        //현재날짜 가져오기
        var Now: Long
        var Date: Date
        val Format1 = SimpleDateFormat("yyyyMMdd")
        val Format2 = SimpleDateFormat("MM/dd")

        Now = System.currentTimeMillis()
        Date = Date(Now)
        sendDate = Format1.format(Date)
        showDate = Format2.format(Date)
        Log.e("태그","sendDate:"+sendDate+" showDate:"+showDate)

        //수면일기 제목 setText
        binding.dateTextView.text = showDate+" 수면일기"
    }

    //파이어베이스에 수면일기 저장 작업
    fun saveSleepData(){
        val sleepdata= hashMapOf(
            "emoji" to emoji,
            "gotobed_time" to gotobed_time,
            "sleep_time" to sleep_time,
            "sleep_peroid" to sleep_peroid,
            "wakeup_time" to wakeup_time,
            "outtobedtime" to outtobedtime,
            "wakeup_num" to wakeup_num,
            "daysleep_peroid" to daysleep_peroid,
            "coffee" to coffee,
            "beer" to beer,
            "soju" to soju,
            "makguli" to makguli,
            "wine" to wine,
        )
        //firestore에 수면일기 데이터 입력 작업
        firestore?.collection("Data")?.document(firebaseAuth?.currentUser?.email!!)?.collection("sleepdata")?.document(sendDate!!)?.set(sleepdata)
            ?.addOnSuccessListener {
                // 성공할 경우
                Toast.makeText(this, "데이터가 추가되었습니다", Toast.LENGTH_SHORT).show()
                Log.e("태그", "sleepdata:"+sleepdata)
            }
            ?.addOnFailureListener {
                // 실패할 경우
                Toast.makeText(this, "실패하였습니다", Toast.LENGTH_SHORT).show()
            }
    }


    //수면일기 입력값 작성시 작업
    fun Writediary(){
        gotobed_time = binding.gotobedtimeEdittextView.text.toString()
        sleep_time =binding.sleepclockEdittextView.text.toString()
        sleep_peroid = binding.sleeptimeEdittextView.text.toString()
        wakeup_time = binding.waketimeEdittextView.text.toString()
        outtobedtime =binding.outtobedtimeEdittextView.text.toString()

        if(binding.wakenumberEdittextView.text.isEmpty()){
            wakeup_num = 0
        }else{
            wakeup_num = binding.wakenumberEdittextView.text.toString().toInt()
        }
        daysleep_peroid =binding.afternoonsleepEdittextView.text.toString()

        Log.e("태그", "gotobed_time:"+gotobed_time + "sleep_time:"+sleep_time+"sleep_peroid:"+sleep_peroid+
        "wakeup_time:"+wakeup_time+" outtobedtime:"+outtobedtime+" wakeup_num: "+wakeup_num
        +"daysleep_peroid:"+daysleep_peroid)

    }

    //태그선택시 색상변경 작업
    fun ChooseTagEvent(){
        //커피태그 클릭시
        binding.coffeetag0.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.coffeetag1.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag2.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag3.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag4.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag5.setBackgroundResource(R.drawable.button3_round)
            coffee = 1    //coffee변수에 1저장
        }
        binding.coffeetag1.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.coffeetag0.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag2.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag3.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag4.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag5.setBackgroundResource(R.drawable.button3_round)
            coffee = 2
        }
        binding.coffeetag2.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.coffeetag0.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag1.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag3.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag4.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag5.setBackgroundResource(R.drawable.button3_round)
            coffee = 3
        }
        binding.coffeetag3.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.coffeetag0.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag2.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag1.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag4.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag5.setBackgroundResource(R.drawable.button3_round)
            coffee = 4
        }
        binding.coffeetag4.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.coffeetag0.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag2.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag3.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag1.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag5.setBackgroundResource(R.drawable.button3_round)
            coffee = 5
        }
        binding.coffeetag5.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.coffeetag0.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag2.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag3.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag4.setBackgroundResource(R.drawable.button3_round)
            binding.coffeetag1.setBackgroundResource(R.drawable.button3_round)
            coffee = 6
        }

        //맥주
        binding.beertag0.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.beertag1.setBackgroundResource(R.drawable.button3_round)
            binding.beertag2.setBackgroundResource(R.drawable.button3_round)
            binding.beertag3.setBackgroundResource(R.drawable.button3_round)
            binding.beertag4.setBackgroundResource(R.drawable.button3_round)
            binding.beertag5.setBackgroundResource(R.drawable.button3_round)
            beer = 1
        }
        binding.beertag1.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.beertag0.setBackgroundResource(R.drawable.button3_round)
            binding.beertag2.setBackgroundResource(R.drawable.button3_round)
            binding.beertag3.setBackgroundResource(R.drawable.button3_round)
            binding.beertag4.setBackgroundResource(R.drawable.button3_round)
            binding.beertag5.setBackgroundResource(R.drawable.button3_round)
            beer = 2
        }
        binding.beertag2.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.beertag1.setBackgroundResource(R.drawable.button3_round)
            binding.beertag0.setBackgroundResource(R.drawable.button3_round)
            binding.beertag3.setBackgroundResource(R.drawable.button3_round)
            binding.beertag4.setBackgroundResource(R.drawable.button3_round)
            binding.beertag5.setBackgroundResource(R.drawable.button3_round)
            beer =3
        }
        binding.beertag3.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.beertag1.setBackgroundResource(R.drawable.button3_round)
            binding.beertag2.setBackgroundResource(R.drawable.button3_round)
            binding.beertag0.setBackgroundResource(R.drawable.button3_round)
            binding.beertag4.setBackgroundResource(R.drawable.button3_round)
            binding.beertag5.setBackgroundResource(R.drawable.button3_round)
            beer = 4
        }
        binding.beertag4.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.beertag1.setBackgroundResource(R.drawable.button3_round)
            binding.beertag2.setBackgroundResource(R.drawable.button3_round)
            binding.beertag3.setBackgroundResource(R.drawable.button3_round)
            binding.beertag0.setBackgroundResource(R.drawable.button3_round)
            binding.beertag5.setBackgroundResource(R.drawable.button3_round)
            beer = 5
        }
        binding.beertag5.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.beertag1.setBackgroundResource(R.drawable.button3_round)
            binding.beertag2.setBackgroundResource(R.drawable.button3_round)
            binding.beertag3.setBackgroundResource(R.drawable.button3_round)
            binding.beertag4.setBackgroundResource(R.drawable.button3_round)
            binding.beertag0.setBackgroundResource(R.drawable.button3_round)
            beer = 6
        }


        //소주
        binding.sojutag0.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.sojutag1.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag2.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag3.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag4.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag5.setBackgroundResource(R.drawable.button3_round)
            soju = 1
        }
        binding.sojutag1.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.sojutag0.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag2.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag3.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag4.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag5.setBackgroundResource(R.drawable.button3_round)
            soju = 2
        }
        binding.sojutag2.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.sojutag1.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag0.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag3.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag4.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag5.setBackgroundResource(R.drawable.button3_round)
            soju =3
        }
        binding.sojutag3.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.sojutag1.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag2.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag0.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag4.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag5.setBackgroundResource(R.drawable.button3_round)
            soju = 4
        }
        binding.sojutag4.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.sojutag1.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag2.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag3.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag0.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag5.setBackgroundResource(R.drawable.button3_round)
            soju = 5
        }
        binding.sojutag5.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.sojutag1.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag2.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag3.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag4.setBackgroundResource(R.drawable.button3_round)
            binding.sojutag0.setBackgroundResource(R.drawable.button3_round)
            soju = 6
        }

        //탁주
        binding.makgurlitag0.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.makgurlitag1.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag2.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag3.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag4.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag5.setBackgroundResource(R.drawable.button3_round)
            makguli = 1
        }
        binding.makgurlitag1.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.makgurlitag0.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag2.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag3.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag4.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag5.setBackgroundResource(R.drawable.button3_round)
            makguli = 2
        }
        binding.makgurlitag2.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.makgurlitag1.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag0.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag3.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag4.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag5.setBackgroundResource(R.drawable.button3_round)
            makguli = 3
        }
        binding.makgurlitag3.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.makgurlitag1.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag2.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag0.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag4.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag5.setBackgroundResource(R.drawable.button3_round)
            makguli = 4
        }
        binding.makgurlitag4.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.makgurlitag1.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag2.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag3.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag0.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag5.setBackgroundResource(R.drawable.button3_round)
            makguli =5
        }
        binding.makgurlitag5.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.makgurlitag1.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag2.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag3.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag4.setBackgroundResource(R.drawable.button3_round)
            binding.makgurlitag0.setBackgroundResource(R.drawable.button3_round)
            makguli = 6
        }

        //와인
        binding.winetag0.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.winetag1.setBackgroundResource(R.drawable.button3_round)
            binding.winetag2.setBackgroundResource(R.drawable.button3_round)
            binding.winetag3.setBackgroundResource(R.drawable.button3_round)
            binding.winetag4.setBackgroundResource(R.drawable.button3_round)
            binding.winetag5.setBackgroundResource(R.drawable.button3_round)
            wine =1
        }
        binding.winetag1.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.winetag0.setBackgroundResource(R.drawable.button3_round)
            binding.winetag2.setBackgroundResource(R.drawable.button3_round)
            binding.winetag3.setBackgroundResource(R.drawable.button3_round)
            binding.winetag4.setBackgroundResource(R.drawable.button3_round)
            binding.winetag5.setBackgroundResource(R.drawable.button3_round)
            wine =2
        }
        binding.winetag2.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.winetag1.setBackgroundResource(R.drawable.button3_round)
            binding.winetag0.setBackgroundResource(R.drawable.button3_round)
            binding.winetag3.setBackgroundResource(R.drawable.button3_round)
            binding.winetag4.setBackgroundResource(R.drawable.button3_round)
            binding.winetag5.setBackgroundResource(R.drawable.button3_round)
            wine =3
        }
        binding.winetag3.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.winetag1.setBackgroundResource(R.drawable.button3_round)
            binding.winetag2.setBackgroundResource(R.drawable.button3_round)
            binding.winetag0.setBackgroundResource(R.drawable.button3_round)
            binding.winetag4.setBackgroundResource(R.drawable.button3_round)
            binding.winetag5.setBackgroundResource(R.drawable.button3_round)
            wine = 4
        }
        binding.winetag4.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.winetag1.setBackgroundResource(R.drawable.button3_round)
            binding.winetag2.setBackgroundResource(R.drawable.button3_round)
            binding.winetag3.setBackgroundResource(R.drawable.button3_round)
            binding.winetag0.setBackgroundResource(R.drawable.button3_round)
            binding.winetag5.setBackgroundResource(R.drawable.button3_round)
            wine = 5
        }
        binding.winetag5.setOnClickListener {
            it.setBackgroundResource(R.drawable.button2_round)
            binding.winetag1.setBackgroundResource(R.drawable.button3_round)
            binding.winetag2.setBackgroundResource(R.drawable.button3_round)
            binding.winetag3.setBackgroundResource(R.drawable.button3_round)
            binding.winetag4.setBackgroundResource(R.drawable.button3_round)
            binding.winetag0.setBackgroundResource(R.drawable.button3_round)
            wine =6
        }


    } //ChooseTag

}