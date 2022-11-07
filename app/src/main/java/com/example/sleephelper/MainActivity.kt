package com.example.sleephelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.sleephelper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(){
            intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.btnJoin.setOnClickListener(){
            intent = Intent(this, Join::class.java)
            startActivity(intent)
        }
        binding.btnReport.setOnClickListener(){
            intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        binding.btnCalendar.setOnClickListener(){
            intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        binding.btnMypage.setOnClickListener {
            intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        binding.btnRecommendTime.setOnClickListener {
            intent = Intent(this, RecommendTimeActivity::class.java)
            startActivity(intent)
        }

        binding.btnWritingDiary.setOnClickListener {
            intent = Intent(this, WritingDiaryActivity::class.java)
            startActivity(intent)
        }
    }
}