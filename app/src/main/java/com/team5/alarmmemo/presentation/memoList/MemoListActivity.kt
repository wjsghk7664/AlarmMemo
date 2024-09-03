package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivityMemoListBinding

class MemoListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMemoListBinding.inflate(layoutInflater) }
    private var onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            "목록".also { binding.memoListTvTitle.text = it }
            supportFragmentManager.popBackStack("setting", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            binding.memoListBtnBackButton.visibility = View.GONE
            showFragment(ListFragment())

            if (System.currentTimeMillis() - backPressedTime <= 2000) {
                finish()
            } else {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(this@MemoListActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        showFragment(ListFragment())

        initView()
    }

    private fun initView() = with(binding) {
        memoListBtnBackButton.setOnClickListener {
//            "목록".also { memoListTvTitle.text = it }
//            memoListBtnBackButton.visibility = View.GONE
//
//            showFragment(ListFragment())
            onBackPressedCallback.handleOnBackPressed()
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
                R.id.navigation_oss -> {
                    startActivity(Intent(this@MemoListActivity, OssLicensesMenuActivity::class.java))
                    true
                }

                R.id.navigation_profile -> {
                    "프로필".also { binding.memoListTvTitle.text = it }
                    showFragment(ProfileFragment())
                    memoListBtnBackButton.visibility = View.VISIBLE
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }

                else -> false
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
        supportFragmentManager.popBackStack("setting", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.memoList_fg_fragment_view, fragment)
            .addToBackStack("setting")
            .commit()
    }
}
