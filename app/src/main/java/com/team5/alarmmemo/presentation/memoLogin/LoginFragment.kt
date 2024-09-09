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
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.databinding.FragmentLoginBinding
import com.team5.alarmmemo.presentation.memoList.MemoListActivity
import com.team5.alarmmemo.presentation.memoList.MemoListFragment
import com.team5.alarmmemo.presentation.memoSignUp.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewmodel:LoginViewModel by activityViewModels()

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

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

                viewmodel.login(email,displayName)
            }else{
                Log.d("구글 결과코드",result.resultCode.toString())
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
    }

    fun initView() = with(binding){

        loginBtn.setOnClickListener {
            val id = loginUsernameEt.text.toString()
            val password = loginPasswordEt.text.toString()
            viewmodel.login(id,name="",password)
        }

//        loginKakaoLogin.setOnClickListener {
//            kakaoLogin()
//        }

        loginNaverLoginIv.setOnClickListener {
            naverLogin()
        }

//        loginGoogleLogin.setOnClickListener {
//            googleLogin()
//        }

        loginNonLogin.setOnClickListener {
            startActivity(Intent(requireActivity(), MemoListActivity::class.java))
        }

        loginSignUpBtn.setOnClickListener {
            startActivity(Intent(requireActivity(),SignUpActivity::class.java))
        }


    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewmodel.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                    }
                    is UiState.Success -> {
                        Toast.makeText(requireContext(), "로그인 성공", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireActivity(), MemoListActivity::class.java).apply { putExtra("user",state.data) })
                        requireActivity().finish()
                    }
                    is UiState.Failure -> {
                        // 로그인 실패 시 처리
                        when (state.e) {
                            "wrong id" -> Toast.makeText(requireContext(), "아이디가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            "wrong password" -> Toast.makeText(requireContext(), "비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
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

    private fun googleLogin(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }


    private fun kakaoLogin(){
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                requestUserInfo(token.accessToken)
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
                    requestUserInfo(token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireActivity(), callback = callback) // 카카오 이메일 로그인
        }
    }

    fun requestUserInfo(accessToken:String){
        UserApiClient.instance.me { user, error ->
            if(error!=null){
                Log.d("카카오","정보요청 실패")
            }else if(user!= null){
                val email = user.kakaoAccount?.email
                val name = user.kakaoAccount?.legalName
                if(email!=null){
                    viewmodel.login(email,name?:"")
                }else{
                    Log.d("카카오","이메일 존재x")
                }

            }
        }
    }

    private fun naverLogin(){
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.d("test", "AccessToken : " + NaverIdLoginSDK.getAccessToken())
                Log.d("test", "client id : " + getString(R.string.naver_client_id))
                Log.d("test", "ReFreshToken : " + NaverIdLoginSDK.getRefreshToken())
                Log.d("test", "Expires : " + NaverIdLoginSDK.getExpiresAt().toString())
                Log.d("test", "TokenType : " + NaverIdLoginSDK.getTokenType())
                Log.d("test", "State : " + NaverIdLoginSDK.getState().toString())

                NaverIdLoginSDK.getAccessToken()?.let {
                    requestNaverUserData(it)
                }

            }


            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.e("test", "$errorCode $errorDescription")
                Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
                Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
    }

    private fun requestNaverUserData(accessToken: String){
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://openapi.naver.com/v1/nid/me")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(requireContext(), "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let{
                    Log.d("네이버",it)
                    val json = JSONObject(it).getJSONObject("response")
                    val name = json.getString("name")
                    val email = json.getString("email")
                    viewmodel.login(email,name)
                }
            }

        })
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