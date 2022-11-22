package com.example.sleephelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sleephelper.databinding.ActivityRecommendTimeBinding

class RecommendTimeActivity : AppCompatActivity() {

    private var binding: ActivityRecommendTimeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendTimeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setBottomNavigation()

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