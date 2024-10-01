package com.team5.alarmmemo.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.team5.alarmmemo.R
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.databinding.FragmentLoginBinding
import com.team5.alarmmemo.presentation.login.LoginViewModel
import com.team5.alarmmemo.presentation.memoList.MemoListActivity
import com.team5.alarmmemo.presentation.memoList.MemoListFragment
import com.team5.alarmmemo.presentation.signUp.SignUpActivity
import com.team5.alarmmemo.util.AccountUtil.hashPassword
import com.team5.alarmmemo.util.AccountUtil.showToast
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

    private val viewmodel: LoginViewModel by activityViewModels()

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        val isInit = requireActivity().intent.getBooleanExtra("isInit",true)
        if(isInit){
            viewmodel.autoLogin()
        }


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

        loginTvGuestLogin.isEnabled = false
        loginIvNaverLogin.isEnabled = false
        loginCbPrivacy.setOnCheckedChangeListener { buttonView, isChecked ->
            viewmodel.setAgreement(isChecked)

            if(!isChecked){
                loginTvGuestLogin.isEnabled = false
                loginIvNaverLogin.isEnabled = false
            }else{
                loginTvGuestLogin.isEnabled = true
                loginIvNaverLogin.isEnabled = true
            }
        }




        loginTvPrivacyDialog.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setView(WebView(requireActivity()).apply { loadUrl("https://sites.google.com/view/alarmmemo-privacypolicy?usp=sharing") })
                .setNegativeButton("나가기"){ dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        loginBtn.setOnClickListener {
            val id = loginUsernameEt.text.toString()
            val password = loginPasswordEt.text.toString()

            if(id.isNotBlank() && password.isNotBlank()){
                viewmodel.login(id,name="", hashPassword(password))
            }else{
                showToast(requireContext(), "회원정보를 입력해주세요.")
            }
        }

//        loginKakaoLogin.setOnClickListener {
//            kakaoLogin()
//        }

        loginIvNaverLogin.setOnClickListener {
            naverLogin()
        }

//        loginGoogleLogin.setOnClickListener {
//            googleLogin()
//        }

        loginTvGuestLogin.setOnClickListener {
            startActivity(Intent(requireActivity(), MemoListActivity::class.java))
        }

        loginTvSignUp.setOnClickListener {
            startActivity(Intent(requireActivity(), SignUpActivity::class.java))
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