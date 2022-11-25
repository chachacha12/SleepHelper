package com.example.sleephelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sleephelper.databinding.ActivityWritingDiaryBinding
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class WritingDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWritingDiaryBinding
    var firebaseAuth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth!!.currentUser?.email

        binding.imageView.setOnClickListener {
            //다이얼로그 객체 생성
            val dlg = EmojiDialog(this)
            //이모지 선택 이벤트처리
            dlg.setOnEmojiClickedListener {
                binding.imageView.setImageResource(it)
            }
            dlg.show()
        }


        binding.checkButton.setOnClickListener{

            val  data= hashMapOf(
                "emoji" to 3,
                "gotobed_time" to "13:00",
                "sleep_time" to "13:00",
                "sleep_peroid" to "13:00",
                "wakeup_time" to "13:00",
                "outtobedtime" to "13:00",
                "wakeup_num" to 5,
                "daysleep_peroid" to "13:00",
                "coffee" to 5,
                "beer" to 5,
                "soju" to 5,
                "makguli" to 5,
                "wine" to 5,
            )

            firestore?.collection("Data")?.document(firebaseAuth?.currentUser?.email!!)?.collection("sleepdata")?.document("20220101")?.set(data)
                ?.addOnSuccessListener {
                    // 성공할 경우
                    Toast.makeText(this, "데이터가 추가되었습니다", Toast.LENGTH_SHORT).show()

                }
                ?.addOnFailureListener {
                    // 실패할 경우
                    Toast.makeText(this, "데이터가 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
        }


    }




}