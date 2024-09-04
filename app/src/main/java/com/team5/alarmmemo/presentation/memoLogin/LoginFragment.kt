package com.example.alarmmemo.presentation.memoLogin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.ListFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
            val isCache = loginCbAutologin.isChecked
            viewmodel.loginCheck(id,password,isCache)
        }

        loginKakaoLogin.setOnClickListener {

        }

        loginNaverLogin.setOnClickListener {

        }

        loginGoogleLogin.setOnClickListener {

        }

        loginNonLogin.setOnClickListener {
//            parentFragmentManager.beginTransaction().replace(R.id.main, ListFragment.newInstance()).addToBackStack(null).commit()
        }

        loginSignUpBtn.setOnClickListener {
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
//            parentFragmentManager.beginTransaction().replace(R.id.main, SignUpFragment.newInstance()).addToBackStack(null).commit()
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