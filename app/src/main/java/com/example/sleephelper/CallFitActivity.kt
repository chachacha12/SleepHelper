package com.example.sleephelper

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.renderscript.Element
import android.system.Os.access
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.bumptech.glide.load.engine.executor.GlideExecutor.UncaughtThrowableStrategy.LOG
import com.example.sleephelper.databinding.ActivityCallFitBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.data.DataType.TYPE_SLEEP_SEGMENT
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.google.android.gms.fitness.result.SessionReadResponse
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit


// Android 권한 요청 -> 데이터 엑세스 권한 확인 -> Oauth 요청 -> 권한 받아 액세스의 과정으로 진행

class CallFitActivity : AppCompatActivity() {

    lateinit var binding: ActivityCallFitBinding
    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    }

    //fitnessOptions 객체 생성 - 수면 데이터를 읽을것임을 선언
    private val fitnessOptions =
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build()

    private val SLEEP_STAGE = arrayOf(
        "불용데이터",
        "누워있지만 깨어있음",
        "수면",
        "침대에서 나옴",
        "얕은 수면",
        "깊은 수면",
        "렘 수면"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallFitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAuth()
        checkPermission()

    }


    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)// Permission is not granted
        }
    }

    private fun checkAuth(){
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            accessGoogleFit()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> accessGoogleFit()
                else -> {
                    Log.i(TAG, "Result wasn't from Google Fit")
                }
            }
            else -> {
                Log.i(TAG, "Result wasn't from Google Fit")
            }
        }
    }


    // onActivityResult에서 승인 확인 받았을때 실행 fitnessClient로 데이터 기록
    private fun accessGoogleFit() {
        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_SLEEP_SEGMENT)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
        Log.e("LOG", "check5")

        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // Use response data here
                Log.i(TAG, "OnSuccess()")
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e -> Log.e("Log", "check6", e) }
    }

    // 데이터 액세스는 완료 이제 데이터를 읽어오고 기록해야함


//    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)




      // 피트니스 데이터 구독 - 수면 segment API 지원 끊김 여기부터 이용불가



//
//    private fun readSleepData(){
//        val nowTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val startTime = LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val sessionsClient = Fitness.getSessionsClient(this,
//            getGoogleAccount()
//        )
//        val request = SessionReadRequest.Builder()
//            .readSessionsFromAllApps()
//            .includeSleepSessions()
//            .read(DataType.TYPE_SLEEP_SEGMENT)
//            .setTimeInterval(startTime, nowTime, TimeUnit.MILLISECONDS)
//            .build()
//
//        sessionsClient.readSession(request)
//            .addOnSuccessListener { response ->
//                for (session in response.sessions) {
//                    val sessionStart = session.getStartTime(TimeUnit.MILLISECONDS)
//                    val sessionEnd = session.getEndTime(TimeUnit.MILLISECONDS)
//                    Log.i(TAG, "Sleep between $sessionStart and $sessionEnd")
//
//                    // If the sleep session has finer granularity sub-components, extract them:
//                    val dataSets = response.getDataSet(session)
//                    for (dataSet in dataSets) {
//                        for (point in dataSet.dataPoints) {
//                            val sleepStageVal =
//                                point.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
//                            val sleepStage = SLEEP_STAGE[sleepStageVal]
//                            val segmentStart = point.getStartTime(TimeUnit.MILLISECONDS)
//                            val segmentEnd = point.getEndTime(TimeUnit.MILLISECONDS)
//                            binding.stage.setText(sleepStage)
//                            binding.start.setText(segmentStart.toString())
//                            binding.end.setText(segmentEnd.toString())
//                            Log.i(TAG, "\t* Type $sleepStage between $segmentStart and $segmentEnd")
//                        }
//                    }
//                }
//            }.addOnFailureListener {
//                    e ->
//                Log.e("googleFit", "Failed to read session error :$e", e)
//            }
//    }
//

}