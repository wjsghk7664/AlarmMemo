package com.team5.alarmmemo.presentation.memoSignUp

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import com.team5.alarmmemo.Constants.IS_MAKE_TEMP_ACCOUNT
import com.team5.alarmmemo.Constants.PREFS_NAME
import com.team5.alarmmemo.Constants.USER_INPUT_EMAIL

object SignUpUtils {
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

    // 이메일 인증 수신 여부 저장
    fun putMakeTempAccountHistory(context: Context, isMakeTempAccount: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(IS_MAKE_TEMP_ACCOUNT, isMakeTempAccount) }
    }

    // 이메일 인증 수신 여부 불러오기
    fun getMakeTempAccountHistory(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_MAKE_TEMP_ACCOUNT, false)
    }

    // 이메일 인증 시 유저가 작성한 이메일 저장
    fun putUserInputEmail(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(USER_INPUT_EMAIL, email) }
    }

    // 이메일 인증 시 유저가 작성한 이메일 불러오기
    fun getUserInputEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_INPUT_EMAIL, null)
    }


//    // 임의 비밀번호 생성
//    fun generateRandomPassword(length: Int): String {
//        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#\$%^&+=!"
//        return (1..length).map { chars.random() }.joinToString("")
//    }

    // 이메일 유효성 검사
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }

    // 비밀번호 유효성 검사
    fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,20}$")
        return passwordRegex.matches(password)
    }

}