package com.example.sleephelper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sleephelper.databinding.ActivityLoginBinding
import com.example.sleephelper.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class Login : AppCompatActivity() {
    //private lateinit var binding: ActivityLoginBinding
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var startGoogleLoginForResult: ActivityResultLauncher<Intent>
    private var signInButton: SignInButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // auth = Firebase.auth // 위의 var도 auth를 firebaseAuth로 고침
        auth = FirebaseAuth.getInstance()

        googleInit()

        updateUI(auth.currentUser) // 로그인 정보 있을 때 바로 화면 넘어가기
        binding.signInButton.setOnClickListener{
            val signInIntent = mGoogleSignInClient.signInIntent
            startGoogleLoginForResult.launch(signInIntent)
        }
    }

    private fun googleInit() {
       // val default_web_client_id = ""; // Android id X

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        startGoogleLoginForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.let { data ->

                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            val account = task.getResult(ApiException::class.java)!!
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                            println(account.idToken)
                            firebaseAuthWithGoogle(account.idToken!!)
                        } catch (e: ApiException) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e)
                        }
                    }
                    // Google Login Success
                } else {
                    Log.e(TAG, "Google Result Error ${result}")
                }
            }
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        println(credential)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success") // 여기까지 오는데 4분 걸림
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // FirebaseUser 데이터에 따른 UI 작업
        // binding.userName.text = user?.displayName
        // Login 이후 (첫 화면)CalendarActivity로 화면 이동
         if (user != null) {
            intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
            //finish()
         }
    }

    companion object {
        const val TAG = "MainActivity"
    }

} // End





