package com.example.alarmmemo.presentation.memoList

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.ActivityMemoListBinding

class MemoListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMemoListBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showFragment(ListFragment())

        with(binding) {
            MemoListTvList.setOnClickListener {
                with(binding) {
                    "목록".also { MemoListTvTitle.text = it }
                    MemoListLlNewDropdownMenu.visibility = View.VISIBLE
                    MemoListLlDropdownMenu.visibility = View.VISIBLE
                    backButton.visibility = View.GONE
                }
            }

            MemoListTvSetting.setOnClickListener {
                with(binding) {
                    "설정".also { MemoListTvTitle.text = it }
                    MemoListLlDropdownMenu.visibility = View.GONE
                    backButton.visibility = View.VISIBLE
                }
                showFragment(SettingFragment())
            }

            MemoListTvList2.setOnClickListener {
                with(binding) {
                    MemoListLlNewDropdownMenu.visibility = View.GONE
                    MemoListLlDropdownMenu.visibility = View.GONE
                }
                listFragment(2)
            }

            MemoListTvList3.setOnClickListener {
                with(binding) {
                    MemoListLlNewDropdownMenu.visibility = View.GONE
                    MemoListLlDropdownMenu.visibility = View.GONE
                }
                listFragment(3)
            }

            backButton.setOnClickListener {
                "목록".also { MemoListTvTitle.text = it }
                backButton.visibility = View.GONE
                showFragment(ListFragment())
            }
        }

        var check = false
        binding.MemoListIvSettingButton.setOnClickListener {
            if (!check) {
                binding.MemoListLlDropdownMenu.visibility = View.VISIBLE
                check = true
            } else {
                binding.MemoListLlDropdownMenu.visibility = View.GONE
                check = false
            }
        }
    }

    private fun listFragment(argument : Int) {
        val fragment = ListFragment().apply {
            arguments = Bundle().apply {
                putInt("spanCount", argument)
            }
        }

        showFragment(fragment)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.MemoList_fg_fragment_view, fragment)
            .addToBackStack(null)
            .commit()
    }
}