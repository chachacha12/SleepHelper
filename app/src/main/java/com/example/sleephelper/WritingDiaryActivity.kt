package com.example.sleephelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.sleephelper.databinding.ActivityWritingDiaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class WritingDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWritingDiaryBinding
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)



        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        auth!!.currentUser?.email

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

            var sleepdiary = Sleepdiary(3, "13:00", "13:00", "13:00", "13:00", "13:00",
                5, "13:00", 2,2,2,2,2)



            val  data= hashMapOf(
                "emoji" to 3,
                "gotobed_time" to ""
            )


            firestore?.collection("Data")?.document("konkukbhs@gmail.com")?.collection("sleepdata")?.document("20220102")?.set(sleepdiary)
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