package com.team5.alarmmemo.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
<<<<<<<< HEAD:app/src/main/java/com/team5/alarmmemo/presentation/MainActivity.kt
import com.team5.alarmmemo.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
========
import com.example.alarmmemo.R
>>>>>>>> 97eaf5ea09c6b7988f415427d664ae0a37d0c0ca:app/src/main/java/com/team5/alarmmemo/presentation/LoginActivity.kt

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}