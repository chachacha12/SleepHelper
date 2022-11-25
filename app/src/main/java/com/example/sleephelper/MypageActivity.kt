package com.example.sleephelper

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
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
   // val itemList = arrayListOf<ListLayout>()    // 리스트 아이템 배열


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



        // 계정 정보 보여주기
        // auth = Firebase.auth : auth 부분 빨간줄 에러 떠서 auth = FirebaseAuth.getInstance() 로 변경
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth!!.currentUser?.email




        // 사용자의 이메일, 이름 추가하기
        // 각자 이름("name")에 해당되는 이름만 바꿔서 실행
        println("구글 이메일 : " + auth?.currentUser?.email!!)
        val  data= hashMapOf(
            "email" to auth?.currentUser?.email!!,
            "name" to "허정인",
        )

        // 여긴 아직 error부분
        // (auth?.currentUser?.email!!)로 시도한 이메일 그대로 들어가는지 확인
        // firestore 컬렉션-도큐먼트 데이터 추가
        // 1분 정도 걸림 
        db?.collection("User")?.document(auth?.currentUser?.email!!)
            ?.set(data)
            ?.addOnSuccessListener {
                // 성공할 경우
                println("데추")
                Toast.makeText(this, "데이터가 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                // 실패할 경우
                println("데추실")
                Toast.makeText(this, "데이터가 추가에 실패하였습니다", Toast.LENGTH_SHORT).show()
            }


        var text_Name : TextView = findViewById (R.id.nameTextView);
        var text_Email : TextView = findViewById (R.id.EmailTextView);


         // 코드로 추가 안 하고 콘솔에서 버튼 추가시 get할 때 에러
        // document(auth?.currentUser?.email!!) : 로그인된 구글 계정으로 매칭시켜서 데이터 가져옴
        // 이름, 이메일 변경하는데 10초 정도 걸림
        // document(auth?.currentUser?.email!!) or document("eon7500@gmail.com") 둘 다 가능
        db.collection("User")?.document(auth?.currentUser?.email!!)
            ?.get()
            ?.addOnSuccessListener { result->
                // 성공할 경우
                println("성성")
                val email = result["email"] as String // firestore에서 가져오기
                val name = result["name"] as String

                text_Name.setText(name);
                text_Email.setText(email);

                Toast.makeText(this, "성공했습니다", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                // 실패할 경우
                println("실실")
                Toast.makeText(this, "실패하였습니다", Toast.LENGTH_SHORT).show()
            }


      //  println("동작 되는중")

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