package com.team5.alarmmemo.util

import android.content.Context
import android.graphics.Rect
import android.util.Patterns
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import java.security.MessageDigest
import androidx.core.graphics.Insets

object AccountUtil {
    // 토스트 메시지 띄우기
    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    // 초(Int) -> 분:초(String) 형태로 포맷팅
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

//    // 임의 비밀번호 생성
//    fun generateRandomPassword(length: Int): String {
//        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#\$%^&+=!"
//        return (1..length).map { chars.random() }.joinToString("")
//    }


    // 이메일 유효성 검사
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 비밀번호 유효성 검사
    fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,20}$")
        return passwordRegex.matches(password)
    }

    // 비밀번호 암호화
    fun hashPassword(password: String): String {
        val data = password.toByteArray()
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hashValue = sha256.digest(data)
        return hashValue.joinToString("") { "%02x".format(it) }
    }

    // 키보드 스크롤 액션
    fun setKeyboardScorllAciton(root: View, btnContainer: LinearLayout, systemBars:Insets) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)

            val height = root.height
            val keypadHeight = height - rect.bottom

            val layoutParams = btnContainer.layoutParams as ConstraintLayout.LayoutParams

            if (keypadHeight > height * 0.15) {
                btnContainer.setPadding(0, 80, 0, 0)
                layoutParams.bottomMargin = 80 + keypadHeight - systemBars.bottom
                btnContainer.layoutParams = layoutParams
            } else {
                btnContainer.setPadding(0, 0, 0, 0)
                layoutParams.bottomMargin = 80
                btnContainer.layoutParams = layoutParams
            }
        }
    }

}