package com.team5.alarmmemo.presentation.memoList

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.team5.alarmmemo.databinding.ChangeNameDialogBinding
import com.team5.alarmmemo.databinding.FragmentSettingBinding
import com.team5.alarmmemo.presentation.memoLogin.LoginActivity

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            profileIvNameChangeButton.setOnClickListener {
                changeName()
            }

            profileLogoutButton.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)

                Toast.makeText(requireContext(), "로그 아웃 완료!.", Toast.LENGTH_SHORT).show()
            }

            profileDeleteButton.setOnClickListener {
                Toast.makeText(requireContext(), "회원 탈퇴 버튼입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeName() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("이름 변경")

        val dialogBinding = ChangeNameDialogBinding.inflate(LayoutInflater.from(requireContext()))
        builder.setView(dialogBinding.root)

        val listener = DialogInterface.OnClickListener { _, _ ->
            val newName = dialogBinding.dialogInput.text.toString()
            if (newName.isNotEmpty()) {
                saveData(newName)
                binding.profileTvNameText.text = newName
            } else {
                Toast.makeText(requireContext(), "이름을 변경하지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            loadData()
        }

        builder.setPositiveButton("이름 변경", listener)
        builder.setNegativeButton("취소", null)

        builder.show()
    }

    private fun saveData(newName: String) {
        val preferences = requireContext().getSharedPreferences("newName", Context.MODE_PRIVATE)
        val edit = preferences.edit()
        edit.putString("name", newName)
        edit.apply()
    }

    private fun loadData() {
        val preferences = requireContext().getSharedPreferences("newName", Context.MODE_PRIVATE)
        val saveNewName = preferences.getString("name", "설정")
        binding.profileTvNameText.text = saveNewName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
