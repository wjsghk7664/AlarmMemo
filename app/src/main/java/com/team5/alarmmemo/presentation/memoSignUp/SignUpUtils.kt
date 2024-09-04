package com.team5.alarmmemo.presentation.memoSignUp

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit

object SignUpUtils {
    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun putEmailSendHistory(context: Context, isSendEmailVerification: Boolean) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("isSendEmailVerification", isSendEmailVerification)}
    }

    fun getEmailSendHistory(context: Context): Boolean{
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("isSendEmailVerification", false)
    }

    fun deleteEmailSendHistory(context: Context) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit { remove("isSendEmailVerification") }
    }
}