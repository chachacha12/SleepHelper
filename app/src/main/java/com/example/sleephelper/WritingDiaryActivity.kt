package com.example.sleephelper

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sleephelper.databinding.ActivityWritingDiaryBinding

class WritingDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWritingDiaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView.setOnClickListener {
            //다이얼로그 객체 생성
            val dlg = EmojiDialog(this)
            //이모지 선택 이벤트처리
            dlg.setOnEmojiClickedListener {
                binding.imageView.setImageResource(it)
            }
            dlg.show()
        }
    }



}