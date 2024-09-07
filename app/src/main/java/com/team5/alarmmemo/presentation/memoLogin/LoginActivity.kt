package com.team5.alarmmemo.presentation.memoLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.Constants.TAG
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivityLoginBinding
import com.team5.alarmmemo.presentation.memoSignUp.SignUpActivity

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.e(TAG, "로그인 성공 ${token.accessToken}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.fragment_login)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        binding.tvSignUp.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}