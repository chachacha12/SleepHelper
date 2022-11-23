package com.example.sleephelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sleephelper.databinding.ActivityMypageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MypageActivity : AppCompatActivity() {

    // 로그아웃 구현을 위한 변수
    var auth : FirebaseAuth ?= null
    var googleSignInClient : GoogleSignInClient?= null
    // firestore
    //  var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null




    private var binding: ActivityMypageBinding? = null

   // private lateinit var binding : ActivityMainBinding    // 뷰 바인딩
    val db = FirebaseFirestore.getInstance()    // Firestore 인스턴스 선언
    val itemList = arrayListOf<ListLayout>()    // 리스트 아이템 배열
    // val adapter = ListAdapter(itemList)         // 리사이클러 뷰 어댑터


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_mypage)
        setContentView(binding?.root)
        setBottomNavigation()



        // 구글 로그아웃을 위해 로그인 세션 가져오기
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        // 구글 로그아웃 버튼 ID 가져오기
        val logoutbtn  = findViewById(R.id.LogoutTextView) as TextView

        // 구글 로그아웃 버튼 클릭 시 이벤트
        logoutbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            googleSignInClient?.signOut()

            var logoutIntent = Intent (this, MainActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(logoutIntent)
        }

            // 알림
        println("로그아웃 됨")


        // 계정 정보 보여주기
        // auth = Firebase.auth
        // auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()




        // binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
       // binding.rvList.adapter = adapter


        var text_Name : TextView = findViewById (R.id.nameTextView);
        text_Name.setText("ㅇㅇ");

        println("동작 안 된다")
        // 로그인 계정 넣기
        firestore!!.collection("User")   // 작업할 컬렉션
            .get()      // 문서 가져오기
            .addOnSuccessListener { result ->
                // 성공할 경우
                println("itemlist 되는가")
               // itemList.clear()
                // for문부터 안 돌아감
                for (document in result) {  // 가져온 문서들은 result에 들어감
                   // val item = arrayListOf<String>(document["email"] as String, document["name"] as String)
                    println("동작되는가요")
                    val email = document["email"] as String
                    val name = document["name"] as String

                    println("동작")
                    println(email)
                    println(name)
                }


                // adapter.notifyDataSetChanged()  // 리사이클러 뷰 갱신
            }
            .addOnFailureListener{
                exception ->
            // 실패할 경우
            Log.w("MainActivity", "Error getting documents: $exception")
        }

//        firestore!!.collection("User").document("konkukbhs@gmail.com").get()      // 문서 가져오기
//            .addOnSuccessListener { result ->
//                // 성공할 경우
//                Log.e("로그", result.get("email").toString())
//                Log.e("로그", result.get("name").toString())
//            }
//            .addOnFailureListener { exception ->
//                // 실패할 경우
//                Log.w("MainActivity", "Error getting documents: $exception")
//            }


    }

    private fun setBottomNavigation() {
        binding!!.bottomNavigation.setOnItemSelectedListener(){
            when(it.itemId){
                R.id.nav_calendar -> {
                    intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_alarm -> {
                    intent = Intent(this, RecommendTimeActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_report -> {
                    intent = Intent(this, ReportActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }









}