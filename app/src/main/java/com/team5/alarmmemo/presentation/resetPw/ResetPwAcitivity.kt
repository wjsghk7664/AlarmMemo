package com.team5.alarmmemo.presentation.resetPw

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.team5.alarmmemo.R
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.databinding.ActivityResetPwBinding
import com.team5.alarmmemo.presentation.login.LoginActivity
import com.team5.alarmmemo.util.AccountUtil.setKeyboardScorllAciton
import com.team5.alarmmemo.util.AccountUtil.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPwAcitivity : AppCompatActivity() {

    val binding by lazy { ActivityResetPwBinding.inflate(layoutInflater) }
    private val viewModel: ResetPwViewModel by viewModels()
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
            setKeyboardScorllAciton(binding.root, binding.resetPwLlBtnContainer, systemBars)
            insets
        }

        context.onBackPressedDispatcher.addCallback(context, callback)
        
        binding.apply {
            resetPwBtnEmailVerification.setOnClickListener {
                val email = resetPwEtEmail.text.toString()
                viewModel.emailVerification(email)
            }

            resetPwBtnSubmit.setOnClickListener {
                val email = binding.resetPwEtEmail.text.toString()
                val password = binding.resetPwEtPassword.text.toString()
                val passwordCheck = binding.resetPwEtPasswordCheck.text.toString()

                viewModel.resetPwSubmitBtnAction(email, password, passwordCheck)
            }

            resetPwBtnCancel.setOnClickListener {
                viewModel.deleteTempAccount()
                finish()
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {

                    launch {
                        viewModel.uiState.collect { state ->
                            when (state) {
                                is UiState.Init -> {
                                    resetPwBtnEmailVerification.isEnabled = true
                                    resetPwEtEmail.isEnabled = true
                                }
                                is UiState.Loading -> {
                                    resetPwEtEmail.isEnabled = false
                                    resetPwBtnEmailVerification.isEnabled = false
                                }
                                is UiState.Success -> {
                                    val event = state.data
                                    if (event == ResetPwEvent.EMAIL_VERIFICATION) {
                                        viewModel.deleteTempAccount()
                                        resetPwTvEmailValidation.setTextColor(
                                            ContextCompat.getColor(context, R.color.green)
                                        )
                                    } else if (event == ResetPwEvent.RESET_PASSWORD) {
                                        resetPwBtnSubmit.isEnabled = false
                                        showToast(context, getString(R.string.reset_pw_submit))
                                        startActivity(Intent(context, LoginActivity::class.java))
                                    }
                                }
                                is UiState.Failure -> {
                                    val msg = state.e
                                    val splitMsg = msg.split("|").map { it.trim() }
                                    showToast(context, getString(R.string.email_validation_error))
                                    Log.e(splitMsg[0], splitMsg[1])
                                }
                            }
                        }
                    }

                    launch {
                        viewModel.emailValidation.collect { msg ->
                            resetPwTvEmailValidation.text = msg
                        }
                    }

                    launch {
                        viewModel.passwordValidation.collect { msg ->
                            resetPwTvPasswordValidation.text = msg
                        }
                    }

                    launch {
                        viewModel.passwordCheckValidation.collect { msg ->
                            resetPwTvPasswordCheckValidation.text = msg
                        }
                    }
                }
            }
        }
    }
}