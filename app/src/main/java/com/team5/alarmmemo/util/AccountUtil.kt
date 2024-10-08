package com.team5.alarmmemo.util

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Patterns
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import java.security.MessageDigest
import androidx.core.graphics.Insets
import com.team5.alarmmemo.Constants.PRIVACY_POLICY_URL

object AccountUtil {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,20}$")
        return passwordRegex.matches(password)
    }

    fun hashPassword(password: String): String {
        val data = password.toByteArray()
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hashValue = sha256.digest(data)
        return hashValue.joinToString("") { "%02x".format(it) }
    }

    fun setKeyboardScorllAciton(root: View, btnContainer: LinearLayout, systemBars: Insets) {
        val density = root.context.resources.displayMetrics.density
        val bottomMargin = (40 * density).toInt()

        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)

            val height = root.height
            val keypadHeight = height - rect.bottom

            val layoutParams = btnContainer.layoutParams as ConstraintLayout.LayoutParams

            if (keypadHeight > height * 0.15) {
                btnContainer.setPadding(0, bottomMargin, 0, 0)
                layoutParams.bottomMargin = bottomMargin + keypadHeight - systemBars.bottom
                btnContainer.layoutParams = layoutParams
            } else {
                btnContainer.setPadding(0, 0, 0, 0)
                layoutParams.bottomMargin = bottomMargin
                btnContainer.layoutParams = layoutParams
            }
        }
    }

    fun showPrivacyPolicyDialog(context: Context) {
        val progressBar = ProgressBar(context)

        val webView = WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    progressBar.visibility = View.GONE
                }
            }
            loadUrl(PRIVACY_POLICY_URL)
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(progressBar)
            addView(webView)
        }

        val dialog = AlertDialog.Builder(context).setView(layout).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}