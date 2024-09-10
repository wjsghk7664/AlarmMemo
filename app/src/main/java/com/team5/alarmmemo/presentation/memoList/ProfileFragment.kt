package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.databinding.ChangeNameDialogBinding
import com.team5.alarmmemo.databinding.FragmentProfileBinding
import com.team5.alarmmemo.presentation.memoLogin.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel:ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user  = requireActivity().intent.getParcelableExtra<User>("user")?:User()

        viewModel.setId(user)
        getState()

        with(binding) {
            profileTvIdText.setText(user.email)
            profileTvNameText.setText(user.name)

            if(user == User()){
                profileDeleteButton.visibility = View.GONE
                profileLogoutButton.text = "로그인"
                profileIvNameChangeButton.isEnabled = false
                profileTvIdText.text = "비회원 로그인 상태입니다."
                profileTvNameText.text = "비회원 로그인 상태입니다."
            }



            // 로그 아웃 버튼 클릭 시
            profileLogoutButton.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()

            }

            // 회원 탈퇴 버튼 클릭 시
            profileDeleteButton.setOnClickListener {
                viewModel.deleteId(user.email)

            }

            // 이름 변경 버튼 클릭 시
            profileIvNameChangeButton.setOnClickListener {
                changeName()
            }
        }
    }

    fun getState() = lifecycleScope.launch {
        viewModel.uiState.collectLatest {
            when(it){
                is UiState.Failure -> Toast.makeText(requireContext(), it.e, Toast.LENGTH_SHORT).show()
                UiState.Init -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    val name = it.data
                    if(name != null){
                        binding.profileTvNameText.setText(name)
                        Toast.makeText(requireContext(), "이름 변경 성공", Toast.LENGTH_SHORT).show()
                        user = user.copy(name = name)
                    }else{
                        Toast.makeText(requireContext(), "회원 탈퇴 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    // 이름 변경 다이얼로그 띄우는 메소드
    private fun changeName() {
        val dialogBinding = ChangeNameDialogBinding.inflate(LayoutInflater.from(requireContext()))

        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("이름 변경")
            setView(dialogBinding.root)

            setPositiveButton("변경") { dialog, _ ->
                val newName = dialogBinding.nameChangeEtDialog.text.toString()
                viewModel.modifyUserData(user.copy(name = newName))
                dialog.dismiss()
            }

            setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}