package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ChangeNameDialogBinding
import com.team5.alarmmemo.databinding.FragmentProfileBinding
import com.team5.alarmmemo.presentation.memoLogin.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            // 로그 아웃 버튼 클릭 시
            profileLogoutButton.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)

                Toast.makeText(requireContext(), getString(R.string.profile_logout_message), Toast.LENGTH_SHORT).show()
            }

            // 회원 탈퇴 버튼 클릭 시
            profileDeleteButton.setOnClickListener {
                Toast.makeText(requireContext(), getString(R.string.profile_delete_message), Toast.LENGTH_SHORT).show()
            }

            // 이름 변경 버튼 클릭 시
            profileIvNameChangeButton.setOnClickListener {
                changeName()
            }

            // 비밀번호 변경 버튼 클릭 시
            profileIvPasswordChangeButton.setOnClickListener {
                Toast.makeText(requireContext(), getString(R.string.profile_password_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                binding.profileTvNameText.text = newName
                Toast.makeText(requireContext(), getString(R.string.profile_name_message), Toast.LENGTH_SHORT).show()
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
