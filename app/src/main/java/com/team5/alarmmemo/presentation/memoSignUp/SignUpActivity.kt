package com.team5.alarmmemo.presentation.memoSignUp

import android.content.Intent
import android.graphics.Rect
import androidx.core.graphics.Insets
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivitySignUpBinding
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.presentation.memoLogin.LoginActivity
import com.team5.alarmmemo.util.AccountUtil.setKeyboardScorllAciton
import com.team5.alarmmemo.util.AccountUtil.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    private val viewModel: SignUpViewModel by viewModels()
    private val context = this

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.deleteTempAccount()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, systemBars.bottom)
            setKeyboardScorllAciton(binding.root, binding.signUpLlBtnContainer, systemBars)
            insets
        }

        viewModel.loginAndDeleteTempAccount()
        context.onBackPressedDispatcher.addCallback(context, callback)

        binding.apply {

            signUpCbPrivacy.setOnCheckedChangeListener { _, isChecked ->
                signUpBtnSubmit.isEnabled = isChecked
            }

            signUpTvCheckDoc.setOnClickListener {
                AlertDialog.Builder(context)
                    .setView(WebView(context).apply { loadUrl("https://sites.google.com/view/alarmmemo-privacypolicy?usp=sharing") })
                    .setNegativeButton("나가기"){ dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }

            signUpBtnEmailVerification.setOnClickListener {
                val email = signUpEtEmail.text.toString()
                viewModel.emailVerificationAction(email)
            }

            signUpBtnSubmit.setOnClickListener {
                val email = binding.signUpEtEmail.text.toString()
                val name = signUpEtName.text.toString()
                val password = binding.signUpEtPassword.text.toString()
                val passwordCheck = binding.signUpEtPasswordCheck.text.toString()

                viewModel.signUpSubmitBtnAction(email, name, password, passwordCheck)
            }

            signUpBtnCancel.setOnClickListener {
                viewModel.deleteTempAccount()
                finish()
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {

                    launch {
                        viewModel.uiState.collect { state ->
                            when (state) {
                                is UiState.Init -> {
                                    signUpBtnEmailVerification.isEnabled = true
                                    signUpEtEmail.isEnabled = true
                                }
                                is UiState.Loading -> {
                                    signUpEtEmail.isEnabled = false
                                    signUpBtnEmailVerification.isEnabled = false
                                }
                                is UiState.Success -> {
                                    val event = state.data
                                    if (event == SignUpSuccessEvent.EMAIL_VERIFICATION_SUCCESS) {
                                        viewModel.deleteTempAccount()
                                        signUpTvEmailValidation.setTextColor(
                                            ContextCompat.getColor(context, R.color.green)
                                        )
                                    } else if (event == SignUpSuccessEvent.SIGN_UP_SUCCESS) {
                                        signUpBtnSubmit.isEnabled = false
                                        showToast(context, getString(R.string.sign_up_submit))
                                        startActivity(Intent(context, LoginActivity::class.java))
                                    }
                                }
                                is UiState.Failure -> {
                                    val msg = state.e
                                    val splitMsg = msg.split("|").map { it.trim() }
                                    showToast(context, getString(R.string.sign_up_email_validation_error))
                                    Log.e(splitMsg[0], splitMsg[1])
                                }
                            }
                        }
                    }

                    launch {
                        viewModel.emailValidation.collect { msg ->
                            signUpTvEmailValidation.text = msg
                        }
                    }

                    launch {
                        viewModel.nameValidation.collect { msg ->
                            signUpTvNameValidation.text = msg
                        }
                    }

                    launch {
                        viewModel.passwordValidation.collect { msg ->
                            signUpTvPasswordValidation.text = msg
                        }
                    }

                    launch {
                        viewModel.passwordCheckValidation.collect { msg ->
                            signUpTvPasswordCheckValidation.text = msg
                        }
                    }
                }
            }

        }
    }
}

