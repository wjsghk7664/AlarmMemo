package com.team5.alarmmemo.presentation.memoSignUp

import android.content.Intent
import android.graphics.Rect
import androidx.core.graphics.Insets
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.team5.alarmmemo.Constants.TEMP_PASSWORD
import com.team5.alarmmemo.R
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.databinding.ActivitySignUpBinding
import com.team5.alarmmemo.Constants.USER
import com.team5.alarmmemo.presentation.memoList.MemoListActivity
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.formatTime
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.getMakeTempAccountHistory
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.getUserInputEmail
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.isValidEmail
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.isValidPassword
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.putMakeTempAccountHistory
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.putUserInputEmail
import com.team5.alarmmemo.presentation.memoSignUp.SignUpUtils.showToast
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SignUpActivity : AppCompatActivity() {

    // 액티비티 바인딩 변수 선언
    val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    // 파이어베이스 관련 인스턴스 지연 초기화 실행
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val store by lazy { FirebaseFirestore.getInstance() }

    private val context = this // 컨텍스트 초기화

    private var isMakeTempAccount = false // 임시계정 생성여부
    private var isEmailVerified = false // 이메일 인증여부

    private val maxTime = 180 // 인증 요휴 기간 (3분)

    private var pollingError = false // 롱 폴링 에러 감지

    private var isPwValid = false // 비밀번호 유효성 검사
    private var isPwCheckValid = false // 비밀번호 확인 유효성 검사

    private lateinit var systemBars: Insets // 화면 여백 정보

    // 뒤로가기 버튼 콜백 정의
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            cancelSignUp()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 바인딩 및 양옆 패딩 설정
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, systemBars.bottom)
            insets
        }

        // 임시 계정 생성 후 홈버튼(reload)을 눌렀을 때 임시 계정 삭제
        if (getMakeTempAccountHistory(context)) {
            cancelSingUpToHomeBtn()
            putMakeTempAccountHistory(context, false)
        }

        // 뒤로가기 버튼 누를 때 콜백호출
        context.onBackPressedDispatcher.addCallback(context, callback)

        // 키패드 감지해서 버튼 레이아웃 하단 패딩 설정
        setKeyboardScorllAciton(binding.root)

        // 바인딩 구문 생략
        binding.apply {

            // 이메일 인증 버튼 동작
            signUpBtnEmailVerification.setOnClickListener {

                val email = signUpEtEmail.text.toString()

                // 이메일 입력 검사
                if (email.isNotBlank()) {

                    // 이메일 형식 검사
                    if (isValidEmail(email)) {

                        // 이메일 중복 체크 및 이메일 인증 요청 수신이 완료되는 시점까지 보여지는 메시지
                        signUpTvEmailValidation.text =
                            getString(R.string.sign_up_email_validation_loading)

                        // 이메일 입력창과 인증 버튼을 비활성화하여
                        // 중복된 이메일 인증 요청과 이메일 변경 방지
                        signUpEtEmail.isEnabled = false
                        signUpBtnEmailVerification.isEnabled = false

                        // 이메일 중복 체크
                        checkEmailDuplicate(email) { isDuplicated, error ->

                            // 이메일 중복 체크를 성공한 경우
                            if (error == null) {

                                // 중복된 이메일이 없는 경우
                                if (!isDuplicated) {

                                    // 입력한 이메일로 임시 계정 생성
                                    createTempAccount(email) { user, error ->

                                        // 임시계정 생성을 성공한 경우
                                        if (error == null) {

                                            // 임시계정 생성기록과 입력한 이메일 저장
                                            isMakeTempAccount = true
                                            putMakeTempAccountHistory(context, true)
                                            putUserInputEmail(context, email)

                                            // 이메일 인증 요청 수신
                                            sendEmailVerification(user) { error ->

                                                // 이메일 인증 요청 수신을 성공한 경우
                                                if (error == null) {

                                                    // 코루틴을 이용해 두 작업 병렬 처리
                                                    lifecycleScope.launch {

                                                        // 백그라운드 상태에서도 동작하는 타이머 작업
                                                        val timerJob = launch {

                                                            var timerRun = true // 타이머 동작 여부
                                                            val startTime =
                                                                System.currentTimeMillis() // 시작 시간

                                                            while (timerRun) {
                                                                val currTime =
                                                                    System.currentTimeMillis() // 현재 시간
                                                                val lapseTime =
                                                                    ((currTime - startTime) / 1000).toInt() // 경과 시간
                                                                val remainTime =
                                                                    maxTime - lapseTime // 남은 시간

                                                                if (remainTime <= 0) timerRun =
                                                                    false // 인증 기간 끝나면 타이머 종료

                                                                signUpTvEmailValidation.text =
                                                                    getString(
                                                                        R.string.sign_up_email_validation_period,
                                                                        formatTime(remainTime)
                                                                    )

                                                                delay(1000L) // 1초 대기
                                                            }
                                                        }

                                                        // 롱 폴링 방식으로 이메일 인증 확인
                                                        val pollingJob = launch {

                                                            var pollingRun = true // 롱 폴링 작업 동작 여부

                                                            while (pollingRun) {

                                                                // 이메일 인증 확인
                                                                checkEmailVerification { complete, error ->

                                                                    // 이메일 인증을 성공한 경우
                                                                    if (error == null) {

                                                                        // 이메일 인증 완료 시
                                                                        if (complete) {
                                                                            // 이메일 인증 플래그 설정 후 두 작업 모두 중지
                                                                            isEmailVerified =
                                                                                complete
                                                                            timerJob.cancel()
                                                                            pollingRun = false
                                                                        }
                                                                    } else {
                                                                        // 이메일 인증을 실패한 경우
                                                                        pollingError = true
                                                                        timerJob.cancel()
                                                                        pollingRun = false
                                                                    }
                                                                }

                                                                delay(2000L) // 2초 대기
                                                            }
                                                        }

                                                        timerJob.join() // 타이머 작업이 끝날 때까지 대기

                                                        if (isEmailVerified) {
                                                            // 이메일 인증을 성공한 경우
                                                            signUpTvEmailValidation.text =
                                                                getString(R.string.sign_up_email_validation_compeleted)

                                                            // 밸리데이션 텍스트 색상 설정
                                                            signUpTvEmailValidation.setTextColor(
                                                                ContextCompat.getColor(
                                                                    context,
                                                                    R.color.green
                                                                )
                                                            )

                                                            // 임시게정 삭제 진행
                                                            deleteTempAccount { error ->
                                                                if (error != null) {
                                                                    // 임시계정 삭제 중 에러가 발생한 경우
                                                                    signUpTvEmailValidation.text =
                                                                        getString(R.string.sign_up_email_validation_error)
                                                                    Log.e(
                                                                        "Firestore",
                                                                        "임시 계정 삭제 에러: ${error}"
                                                                    )

                                                                    // 회원가입을 하지 못하도록 설정
                                                                    isEmailVerified = false
                                                                }
                                                            }
                                                        } else {
                                                            // 이메일 인증을 실패한 경우
                                                            if (pollingError) {
                                                                // 롱폴링 작업 중 에러가 발생한 경우
                                                                signUpTvEmailValidation.text =
                                                                    getString(R.string.sign_up_email_validation_error)
                                                                Log.e(
                                                                    "Auth",
                                                                    "이메일 인증 확인 에러: ${error}"
                                                                )
                                                            } else {
                                                                // 이메일 인증 기간이 만료된 경우
                                                                signUpTvEmailValidation.text =
                                                                    getString(R.string.sign_up_email_validation_expired)
                                                            }
                                                        }
                                                    }

                                                } else {
                                                    // 이메일 인증 요청 수신 중 에러가 발생한 경우
                                                    signUpTvEmailValidation.text =
                                                        getString(R.string.sign_up_email_validation_error)
                                                    Log.e("Auth", "이메일 인증 요청 수신 에러: ${error}")
                                                }
                                            }
                                        } else {
                                            // 임시 계정 생성 중 에러가 발생한 경우
                                            signUpTvEmailValidation.text =
                                                getString(R.string.sign_up_email_validation_error)
                                            Log.e("Auth", "임시 계정 생성 에러: ${error}")
                                        }
                                    }
                                } else {
                                    // 이메일이 중복된 경우
                                    signUpTvEmailValidation.text =
                                        getString(R.string.sign_up_email_validation_duplicated)

                                    // 이메일을 인증을 다시 진행할 수 있도록 설정
                                    signUpBtnEmailVerification.isEnabled = true
                                    signUpEtEmail.isEnabled = true
                                }
                            } else {
                                // 이메일이 중복 체크 중 에러가 발생한 경우
                                signUpTvEmailValidation.text =
                                    getString(R.string.sign_up_email_validation_error)
                                Log.e("Firestore", "이메일 중복 체크 에러: ${error}")
                            }
                        }
                    } else {
                        // 이메일 형식이 알맞지 않은 경우
                        signUpTvEmailValidation.text =
                            getString(R.string.sign_up_email_invalid_format)
                    }
                } else {
                    // 이메일이 공백인 경우
                    signUpTvEmailValidation.text =
                        getString(R.string.sign_up_email_empty)
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
                            getString(R.string.sign_up_password_empty)
                    } else if (!isValidPassword(password)) {
                        signUpTvPwValidation.text =
                            getString(R.string.sign_up_password_invalid_format)
                    } else {
                        signUpTvPwValidation.text = ""
                        isPwValid = true
                    }
                }

                // 비밀번호 확인 유효성 검사
                if (isEmailVerified && isPwValid) {
                    if (password != passwordCheck) {
                        signUpTvPwCheckValidation.text =
                            getString(R.string.sign_up_password_check_error)
                    } else {
                        signUpTvPwCheckValidation.text = ""
                        isPwCheckValid = true
                    }
                }

                // 모든 유효성 검사를 통과하면 회원가입 실행
                if (isEmailVerified && isPwValid && isPwCheckValid) {
                    signUpBtnSubmit.isEnabled = false // 중복된 회원가입 동작 방지

                    createAccount(email, password) { error, uid ->
                        if (error == null) {
                            val user = User(email)
                            addUserToStore(uid, user) { error ->
                                if (error != null) {
                                    showToast(context, getString(R.string.sign_up_submit_error))
                                    Log.e("Firestore", "파이어 스토어 유저 정보 저장 에러: ${error}")
                                }
                            }
                        } else {
                            showToast(context, getString(R.string.sign_up_submit_error))
                            Log.e("Auth", "계정 생성 에러: ${error}")
                        }
                    }

                    showToast(context, getString(R.string.sign_up_submit))
                    startActivity(Intent(context, MemoListActivity::class.java))
                }
            }

            // 회원가입 취소 버튼 동작
            signUpBtnCancel.setOnClickListener {
                cancelSignUp()
                finish()
            }
        }
    }

    // 이메일 중복확인
    private fun checkEmailDuplicate(email: String, callback: (Boolean, String?) -> Unit) {
        val query = store.collection(USER)
            .whereEqualTo("email", email).limit(1).get()

        query.addOnSuccessListener {
            if (!it.isEmpty) {
                callback(true, null)
            } else {
                callback(false, null)
            }
        }.addOnFailureListener {
            callback(false, it.message)
        }
    }

    // 이메일 인증을 위한 임시 계정 생성
    private fun createTempAccount(email: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, TEMP_PASSWORD)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(task.result.user, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    // 이메일 인증 요청 수신
    private fun sendEmailVerification(user: FirebaseUser?, callback: (String?) -> Unit) {
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(null)
            } else {
                callback(task.exception?.message)
            }
        }
    }

    // 이메일 인증 확인
    private fun checkEmailVerification(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(user.isEmailVerified, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    // 임시계정 삭제
    private fun deleteTempAccount(callback: (String?) -> Unit) {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(null)
            } else {
                callback(task.exception?.message)
            }
        }
    }

    // 계정 생성
    private fun createAccount(
        email: String,
        password: String,
        callback: (String?, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(null, task.result.user!!.uid)
                } else {
                    callback(task.exception?.message, "")
                }
            }
    }

    // 파이어스토어에 유저정보 추가
    private fun addUserToStore(uid: String, user: User, callback: (String?) -> Unit) {
        store.collection(USER).document(uid).set(user).addOnSuccessListener {
            callback(null)
        }.addOnFailureListener {
            callback(it.message)
        }
    }

    // 임시계정으로 로그인
    private fun loginTempAccount(email: String, callback: (String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, TEMP_PASSWORD)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(null)
                } else {
                    callback(task.exception?.message)
                }
            }
    }

    // 임시계정 생성 후 회원가입을 중지한 경우 호출
    private fun cancelSignUp() {
        if (isMakeTempAccount) {
            deleteTempAccount { error ->
                if (error != null) Log.e(
                    "Firestore",
                    "임시 계정 삭제 에러: ${error}"
                )
            }
        }
    }

    // 이메일 인증 후 홈버튼을 눌러 회원가입을 중지한 경우 호출
    private fun cancelSingUpToHomeBtn() {
        val email = getUserInputEmail(context)
        if (email != null) {
            loginTempAccount(email) { error ->
                if (error == null) {
                    deleteTempAccount { error ->
                        if (error != null) {
                            Log.e("Auth", "임시 계정 삭제 에러: ${error}")
                        }
                    }
                } else {
                    Log.e("Auth", "임시 계정 로그인 에러: ${error}")
                }
            }
        }
    }

    // 키패드 감지해서 버튼 레이아웃 하단 패딩 설정
    private fun setKeyboardScorllAciton(root: View) {
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
}