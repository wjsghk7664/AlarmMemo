package com.example.alarmmemo.presentation.memoList

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.alarmmemo.ListFragment
import com.example.alarmmemo.R
import com.example.alarmmemo.SettingFragment
import com.example.alarmmemo.databinding.ActivityMemoListBinding

class MeMoListActivity : AppCompatActivity() {

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

        binding.MemoListTvList.setOnClickListener {
            binding.MemoListLlDropdownMenu.visibility = View.GONE
            showFragment(ListFragment())
        }

        binding.MemoListTvSetting.setOnClickListener {
            binding.MemoListLlDropdownMenu.visibility = View.GONE
            showFragment(SettingFragment())
        }

        binding.MemoListIvSettingButton.setOnClickListener {
            toggleDropDown(it)
        }
    }

    private fun toggleDropDown(view: View) {
        if (binding.MemoListLlDropdownMenu.visibility == View.GONE) {
            binding.MemoListLlDropdownMenu.visibility = View.VISIBLE
        } else {
            binding.MemoListLlDropdownMenu.visibility = View.GONE
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.MemoList_fg_fragment_view, fragment)
            .addToBackStack(null)
            .commit()
    }
}