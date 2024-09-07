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

    // 프로필 화면에서 내비게이션 바의 뒤로 가기 했을 때 리스트 화면으로 돌아가도록 설정
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
                Toast.makeText(this@MemoListActivity, R.string.memoList_back_pressed_message, Toast.LENGTH_SHORT).show()
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

        // 기본 화면은 리스트 화면
        showFragment(ListFragment())

        initView()
    }

    private fun initView() = with(binding) {
        // 프로필 화면에서 뒤로 가기 버튼 눌러도 리스트 화면으로 돌아가도록 설정
        memoListBtnBackButton.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // 설정 메뉴 버튼 눌렀을 때 drawer 열리고 닫히도록 설정
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

        // 각 drawer의 메뉴 별로 클릭했을 때 어디로 이동할 지 설정
        memoListNvNavigationView.setNavigationItemSelectedListener { menuItem ->
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

    // 프래그먼트를 새로 생성하면서 보여주는 메소드
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack("setting", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.memoList_fg_fragment_view, fragment)
            .addToBackStack("setting")
            .commit()
    }
}
