package com.team5.alarmmemo.presentation.memoSignUp

import android.content.Intent
import android.graphics.Rect
import androidx.core.graphics.Insets
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivitySignUpBinding
import com.team5.alarmmemo.presentation.memoList.MemoListActivity
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.formatTime
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.getEmailSendHistory
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.putEmailSendHistory
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpActivity : AppCompatActivity() {

    val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    // 파이어베이스 관련 인스턴스 지연초기화 실행
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val store by lazy { FirebaseFirestore.getInstance() }

    private val context = this // 컨텍스트 초기화

    private var isSendEmailValidation = false // 이메일 인증 시도여부
    private var isEmailVerified = false // 이메일 인증여부

    private val maxTime = 180 // 인증 요휴 기간 (3분)

    private var isPwValid = false // 비밀번호 유효성 검사
    private var isPwCheckValid = false // 비밀번호 확인 유효성 검사

    private lateinit var systemBars: Insets // 화면 여백 정보

    // 뒤로가기 버튼 콜백 정의
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            signUpCancel()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 회원가입 액티비티의 바인딩 및 양옆 패딩 설정
        val root = binding.root
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, systemBars.bottom)
            insets
        }

        // 이메일 인증 요청 후 홈버튼을 눌렀을 때의 예외처리
        isSendEmailValidation = getEmailSendHistory(context)
        if (isSendEmailValidation) {
            signUpCancel()
            putEmailSendHistory(context, false)
        }

        // 뒤로가기 버튼 누를 때 콜백호출
        context.onBackPressedDispatcher.addCallback(context, callback)

        // 키패드 감지해서 버튼 레이아웃 하단 패딩 설정
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // 화면 영역을 rect 객체에 지정
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)

            // 키패드 높이 계산
            val height = root.height
            val keypadHeight = height - rect.bottom

            // 키패드가 화면에 나타난 경우와 사라진 경우 처리
            if (keypadHeight > height * 0.15) {
                adjustView(keypadHeight)
            } else {
                resetView()
            }
        }

        // 바인딩 구문 생략
        binding.apply {

            // 이메일 인증 버튼 동작
            signUpBtnEmailVerification.setOnClickListener {

                val email = signUpEtEmail.text.toString()

                // 이메일을 입력했는지 확인
                if (email.isNotBlank()) {

                    // 이메일 유효성 검사
                    if (isValidEmail(email)) {

                        // 이메일 중복 체크 및 이메일 인증 요청 수신이 완료되는 시점까지 보여지는 메시지
                        signUpTvEmailValidation.text =
                            getString(R.string.sign_up_email_validation_loading)

                        // 중복된 이메일 인증 요청과 이메일 변경 방지
                        signUpBtnEmailVerification.isEnabled = false
                        signUpEtEmail.isEnabled = false

                        // 이메일 인증 및 회원가입 처리 비동기로 실행
                        lifecycleScope.launch {

                            // 중복되는 이메일이 없다면
                            if (!checkEmailDuplicate(email)) {

                                // 플래그 설정 후 이메일 인증 요청 수신
                                sendEmailVerificaiton(email)

                                val startTime = System.currentTimeMillis() // 시작 시간 기록

                                // 타이머 ui 출력
                                val timerJob = launch {
                                    while (true) {
                                        val currTime = System.currentTimeMillis() // 현재 시간
                                        val lapseTime =
                                            ((currTime - startTime) / 1000).toInt() // 경과 시간
                                        val remainTime = maxTime - lapseTime // 남은 시간

                                        if (remainTime <= 0) break // 인증 기간 끝나면 타이머 종료

                                        signUpTvEmailValidation.text = getString(
                                            R.string.sign_up_email_validation_period,
                                            formatTime(remainTime)
                                        )

                                        delay(1000L)
                                    }
                                }

                                // 롱 폴링 방식으로 이메일 인증 확인
                                val pollingJob = launch {
                                    while (true) {
                                        checkEmailVerified() // 이메일 인증

                                        // 이메일 인증 완료 시 두 작업 모두 종료
                                        if (isEmailVerified) {
                                            timerJob.cancel()
                                            break
                                        }

                                        delay(2000L)
                                    }
                                }

                                timerJob.join() // 타이머가 끝날 때까지 대기

                                // 제한시간 내 이메일 인증 실패 시
                                if (!isEmailVerified) {
                                    // 이메일 입력창과 인증 버튼을 비활성화 상태로 유지해 회원가입을 다시하도록
                                    signUpTvEmailValidation.text =
                                        getString(R.string.sign_up_email_validation_expired)
                                } else {
                                    signUpTvEmailValidation.text =
                                        getString(R.string.sign_up_email_validation_compeleted)
                                    signUpTvEmailValidation.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.green
                                        )
                                    )

                                    deleteTempAccount() // 이메일 인증 완료 후 임시 계정 삭제
                                }
                            } else {
                                // 사용 중인 이메일로 인증 요청을 보냈을 경우 다시 인증 가능하도록 
                                signUpTvEmailValidation.text =
                                    getString(R.string.sign_up_email_validation_duplicated)
                                signUpBtnEmailVerification.isEnabled = true
                                signUpEtEmail.isEnabled = true
                            }
                        }
                    } else {
                        signUpTvEmailValidation.text =
                            getString(R.string.sign_up_email_validation_invalid_format)
                    }
                } else {
                    signUpTvEmailValidation.text =
                        getString(R.string.sign_up_email_validation_empty)
                }
            }

            // 회원가입 버튼 동작 (순차적으로 유효성 검사 실행)
            signUpBtnSubmit.setOnClickListener {

                // 사용자 입력값 가져오기
                val email = signUpEtEmail.text.toString()
                val password = signUpEtPw.text.toString()
                val passwordCheck = signUpEtPwCheck.text.toString()

                // 이메일 인증 여부 확인
                if (!isEmailVerified) {
                    signUpTvEmailValidation.text =
                        getString(R.string.sign_up_email_validation_request)
                }

                // 비밀번호 유효성 검사
                if (isEmailVerified) {
                    if (password.isBlank()) {
                        signUpTvPwValidation.text =
                            getString(R.string.sign_up_password_validation_empty)
                    } else if (!isValidPw(password)) {
                        signUpTvPwValidation.text =
                            getString(R.string.sign_up_password_validation_invalid_format)
                    } else {
                        signUpTvPwValidation.text = ""
                        isPwValid = true
                    }
                }

                // 비밀번호 확인 유효성 검사
                if (isEmailVerified && isPwValid) {
                    if (password != passwordCheck) {
                        signUpTvPwCheckValidation.text =
                            getString(R.string.sign_up_password_check_validation_error)
                    } else {
                        signUpTvPwCheckValidation.text = ""
                        isPwCheckValid = true
                    }
                }

                // 모든 유효성 검사를 통과하면 회원가입 실행
                if (isEmailVerified && isPwValid && isPwCheckValid) {
                    signUpBtnEmailVerification.isEnabled = false // 중복된 회원가입 동작 방지
                    lifecycleScope.launch { signUp(email, password) }
                    showToast(context, getString(R.string.sign_up_compeleted))
                    startActivity(Intent(context, MemoListActivity::class.java))
                }
            }

            // 회원가입 취소 버튼 동작
            signUpBtnCancel.setOnClickListener {
                signUpCancel()
                finish()
            }
        }
    }

    // 키패드가 나타날 때 버튼 ui 위치조정
    private fun adjustView(keypadHeight: Int) {
        val layoutParams =
            binding.signUpLlBtnContainer.layoutParams as ConstraintLayout.LayoutParams
        binding.signUpLlBtnContainer.setPadding(0, 80, 0, 0)
        layoutParams.bottomMargin = 80 + keypadHeight - systemBars.bottom
        binding.signUpLlBtnContainer.layoutParams = layoutParams
    }

    // 키패드가 사라질 때 버튼 ui 되돌리기
    private fun resetView() {
        val layoutParams =
            binding.signUpLlBtnContainer.layoutParams as ConstraintLayout.LayoutParams
        binding.signUpLlBtnContainer.setPadding(0, 0, 0, 0)
        layoutParams.bottomMargin = 80
        binding.signUpLlBtnContainer.layoutParams = layoutParams
    }

    // 이메일 유효성 검사
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }

    // 이메일 중복 확인
    private suspend fun checkEmailDuplicate(email: String): Boolean {
        val query = store.collection("users")
            .whereEqualTo("email", email).limit(1).get().await()
        return !query.isEmpty
    }

    // 임의 비밀번호 생성
    private fun generateRandomPw(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#\$%^&+=!"
        return (1..length).map { chars.random() }.joinToString("")
    }

    // 이메일 인증 요청 수신 플래그 설정
    private fun setEmailSendFlag() {
        isSendEmailValidation = true
        putEmailSendHistory(context, true)
    }

    // 이메일 인증 요청 (임시 계정 생성 필요)
    private suspend fun sendEmailVerificaiton(email: String) {
        val randomPw = generateRandomPw(8)
        val res = auth.createUserWithEmailAndPassword(email, randomPw).await()
        res.user?.sendEmailVerification()?.await()
        setEmailSendFlag()
    }

    // 이메일 인증 확인
    private suspend fun checkEmailVerified() {
        val currentUser = auth.currentUser
        currentUser?.reload()?.await()
        isEmailVerified = currentUser?.isEmailVerified ?: false
    }

    // 임시계정 삭제
    private suspend fun deleteTempAccount() {
        auth.currentUser?.delete()?.await()
    }

    // 파이어스토어에 유저정보 추가
    private suspend fun addUserToStore(uid: String, email: String) {
        // Todo : user를 데이터 클래스로 초기화 해야함
        val user = hashMapOf("email" to email)
        store.collection("User").document(uid).set(user).await()
    }

    // 임시계정 생성 후 회원가입을 완료하지 않는 경우 호출
    private fun signUpCancel() {
        if (isSendEmailValidation) {
            lifecycleScope.launch {
                deleteTempAccount()
            }
        }
    }

    // 비밀번호 유효성 검사
    private fun isValidPw(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,20}$")
        return passwordRegex.matches(password)
    }

    // 회원가입 동작 정의
    private suspend fun signUp(email: String, password: String) {
        val res = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = res.user?.uid!!
        addUserToStore(uid, email)
    }
}