package com.team5.alarmmemo.presentation.memoLogin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.ListFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.Constants.TAG
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.FragmentLoginBinding
import com.team5.alarmmemo.presentation.memoSignUp.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewmodel:LoginViewModel by activityViewModels()

    private lateinit var resultLauncher : ActivityResultLauncher<Intent>
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 구글 로그인 클라이언트 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        setResultSignUp()
        initView()
        observeViewModel()
    }

    fun initView() = with(binding){
        viewLifecycleOwner.lifecycleScope.launch {
            viewmodel.uiState.collectLatest {
                when(it){
                    is UiState.Failure -> {
                        if(it.e=="fail"){
                            Toast.makeText(requireContext(),"아이디나 비밀번호를 다시 입력해주세요.",Toast.LENGTH_SHORT).show()
                        }else if(it.e=="cachefail"){
                            Toast.makeText(requireContext(),"로그인 정보 저장에 실패하였습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                        }
                    }
                    is UiState.Init -> null
                    is UiState.Loading -> null
                    is UiState.Success -> null //액티비티에서 처리
                    else -> {}
                }
            }
        }

        loginBtn.setOnClickListener {
            val id = loginUsernameEt.text.toString()
            val password = loginPasswordEt.text.toString()
//            val isCache = loginCbAutologin.isChecked
//            viewmodel.loginCheck(id,password,isCache)
        }

        loginKakaoLogin.setOnClickListener {
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                }
            }
            // 카카오톡 설치 확인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireActivity())) {
                // 카카오톡 로그인
                UserApiClient.instance.loginWithKakaoTalk(requireActivity()) { token, error ->
                    // 로그인 실패 부분
                    if (error != null) {
                        Log.e(TAG, "로그인 실패 $error")
                        // 사용자가 취소
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }
                        // 다른 오류
                        else {
                            UserApiClient.instance.loginWithKakaoAccount(
                                requireActivity(),
                                callback = callback
                            ) // 카카오 이메일 로그인
                        }
                    }
                    // 로그인 성공 부분
                    else if (token != null) {
                        Log.e(TAG, "로그인 성공 ${token.accessToken}")
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(requireActivity(), callback = callback) // 카카오 이메일 로그인
            }
        }

        loginNaverLogin.setOnClickListener {
            val oauthLoginCallback = object : OAuthLoginCallback {
                override fun onSuccess() {
                    Log.d("test", "AccessToken : " + NaverIdLoginSDK.getAccessToken())
                    Log.d("test", "client id : " + getString(R.string.naver_client_id))
                    Log.d("test", "ReFreshToken : " + NaverIdLoginSDK.getRefreshToken())
                    Log.d("test", "Expires : " + NaverIdLoginSDK.getExpiresAt().toString())
                    Log.d("test", "TokenType : " + NaverIdLoginSDK.getTokenType())
                    Log.d("test", "State : " + NaverIdLoginSDK.getState().toString())
                }


                override fun onFailure(httpStatus: Int, message: String) {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                    Log.e("test", "$errorCode $errorDescription")
                }
                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }

//            NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
            }
        }

        loginGoogleLogin.setOnClickListener {
//            val signInIntent = googleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)

            val signInIntent = googleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)

        }

        loginNonLogin.setOnClickListener {
//            parentFragmentManager.beginTransaction().replace(R.id.main, ListFragment.newInstance()).addToBackStack(null).commit()
        }

        loginSignUpBtn.setOnClickListener {
//            parentFragmentManager.beginTransaction().replace(R.id.main, SignUpFragment.newInstance()).addToBackStack(null).commit()
        }


    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewmodel.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 로딩 중인 경우 처리
                        Toast.makeText(requireContext(), "로그인 중...", Toast.LENGTH_SHORT).show()
                    }
                    is UiState.Success -> {
                        // 로그인 성공 시 처리
                        Toast.makeText(requireContext(), "로그인 성공", Toast.LENGTH_SHORT).show()
                        // 예: 다음 화면으로 이동
                        // findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    is UiState.Failure -> {
                        // 로그인 실패 시 처리
                        when (state.e) {
                            "wrong id" -> Toast.makeText(requireContext(), "아이디가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            "wrong password" -> Toast.makeText(requireContext(), "비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            "Social Login Init" -> {
                                Toast.makeText(requireContext(), "소셜 로그인 첫 시도입니다. 계정을 생성하세요.", Toast.LENGTH_SHORT).show()
                                // 소셜 로그인 초기화 시 추가 작업 필요
                            }
                            else -> Toast.makeText(requireContext(), "로그인 실패: ${state.e}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is UiState.Init -> {
                        // 초기 상태일 때 처리
                    }
                }
            }
        }
    }

    // 구글로그인 콜백
    private fun setResultSignUp(){
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if(result.resultCode == Activity.RESULT_OK){
                // 로그인 유저정보 불러오기
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                val email = account?.email.toString()
                val familyName = account?.familyName.toString()
                val givenName = account?.givenName.toString()
                val displayName = account?.displayName.toString()
                val photoUrl = account?.photoUrl.toString()
                val id = account?.id.toString()
                val idToken = account?.idToken.toString()

                Log.d(TAG,"이메일 $email\n 이름정보 $familyName $givenName $displayName\n 포토url $photoUrl\n id $id\n idToken $idToken\n")

                val intent = Intent(requireContext(),ListFragment::class.java)
                    .putExtra("google","google")
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}