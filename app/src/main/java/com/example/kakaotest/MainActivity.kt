package com.example.kakaotest

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kakaotest.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import java.security.MessageDigest
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit
    private lateinit var binding: ActivityMainBinding
    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var imageview: ImageView
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
        }
    }

    //구글 로그인
    var googleLoginReult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            var data = result.data
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        }

    fun firebaseAuthWithGoogle(idToken: String?) {
        var credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential((credential)).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)
            }
        }
    }

    /**val keyHash = Utility.getKeyHash(this)
    Log.e("Key","keyHash: $keyHash")
    /** KakaoSDK init */
    KakaoSdk.init(this, this.getString(R.string.kakao_app_key))
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance() //2 FirebaseAuth의 인스턴스 초기화
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, getString(R.string.kakao_app_key))

        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //지도 생성 테스트 버튼
        binding.tmapViewbtn.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // 이메일 회원가입
        val joinBtn = findViewById<Button>(R.id.email_login_button)
        joinBtn.setOnClickListener {

            // email, pwd를 받아오기
            //첫번째 방법
            val id: EditText = findViewById<EditText>(R.id.id_edittext)
            val password = findViewById<EditText>(R.id.password_edittext)

            auth!!.createUserWithEmailAndPassword(id.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //이메일 로그인

        val signBtn = findViewById<Button>(R.id.id_login_button)
        signBtn.setOnClickListener {

            val id: EditText = findViewById<EditText>(R.id.id_edittext)
            val password = findViewById<EditText>(R.id.password_edittext)

            auth!!.signInWithEmailAndPassword(id.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
                    }
                }

        }


        //구글 로그인 버튼
        binding.googleSignInButton.setOnClickListener {
            signIn() //signin으로 이동
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //카카오 로그인 버튼
        binding.kakaoSignInButton.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }
    }

    //구글 signin 화면으로 이동
    private fun signIn() {
        val i = googleSignInClient.signInIntent
        googleLoginReult.launch(i)
    }

    //로그인 activity에서 Main으로
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    //해시 키값 확인용
    private fun getAppKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e(TAG, "해시키 : $hashKey")
            }
        } catch (e: Exception) {
            Log.e(TAG, "해시키를 찾을 수 없습니다 : $e")
        }
    }



}










