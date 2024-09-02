package com.team5.alarmmemo.presentation.memoList

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivityMemoListBinding

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
//            memoListTvList.setOnClickListener {
//                with(binding) {
//                    memoListLlNewDropdownMenu.visibility = View.VISIBLE
//                    memoListLlDropdownMenu.visibility = View.VISIBLE
//                    memoListBtnBackButton.visibility = View.GONE
//                }
//            }
//
//            memoListTvSetting.setOnClickListener {
//                with(binding) {
//                    "설정".also { memoListTvTitle.text = it }
//                    memoListLlDropdownMenu.visibility = View.GONE
//                    memoListBtnBackButton.visibility = View.VISIBLE
//                }
//                showFragment(SettingFragment())
//            }
//
//            memoListTvList2.setOnClickListener {
//                "목록".also { memoListTvTitle.text = it }
//                with(binding) {
//                    memoListLlNewDropdownMenu.visibility = View.GONE
//                    memoListLlDropdownMenu.visibility = View.GONE
//                }
//                listFragment(2)
//            }
//
//            memoListTvList3.setOnClickListener {
//                "목록".also { memoListTvTitle.text = it }
//                with(binding) {
//                    memoListLlNewDropdownMenu.visibility = View.GONE
//                    memoListLlDropdownMenu.visibility = View.GONE
//                }
//                listFragment(3)
//            }

            memoListBtnBackButton.setOnClickListener {
                "목록".also { memoListTvTitle.text = it }
                memoListBtnBackButton.visibility = View.GONE
                showFragment(ListFragment())
            }

            var check = false
            memoListIvSettingButton.setOnClickListener {
                if (!check) {
                    binding.drawerLayout.openDrawer(GravityCompat.END)
                    check = true
                } else {
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    check = false
                }
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_list -> {
                        "목록".also { binding.memoListTvTitle.text = it }
                        showFragment(ListFragment())
                        binding.drawerLayout.closeDrawer(GravityCompat.END)
                        true
                    }
                    R.id.navigation_setting -> {
                        "설정".also { binding.memoListTvTitle.text = it }
                        showFragment(SettingFragment())
                        binding.drawerLayout.closeDrawer(GravityCompat.END)
                        true
                    }
                    else -> false
                }
            }
        }
    }

//    private fun listFragment(spanCount : Int) {
//        val fragment = ListFragment().apply {
//            arguments = Bundle().apply {
//                putInt("spanCount", spanCount)
//                Log.d("ListActivity", spanCount.toString())
//            }
//        }
//
//        showFragment(fragment)
//    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.memoList_fg_fragment_view, fragment)
            .addToBackStack(null)
            .commit()
    }
}