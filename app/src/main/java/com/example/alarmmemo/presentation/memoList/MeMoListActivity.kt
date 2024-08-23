package com.example.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.alarmmemo.ListItem
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.ActivityMemoListBinding
import com.example.alarmmemo.presentation.memo.MemoActivity

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

        val adapter = MenuListAdapter {
            val intent = Intent(this, MemoActivity::class.java)
            startActivity(intent)
        }

        binding.MemoListRvMemoList.layoutManager = GridLayoutManager(this, 2)
        binding.MemoListRvMemoList.adapter = adapter

        val sampleData = listOf(
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher),
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher),
            ListItem("데이터 2", "date2", R.mipmap.ic_launcher),
            ListItem("데이터 3", "date3", R.mipmap.ic_launcher)
        )

        adapter.submitList(sampleData)

        val spinner: Spinner = binding.MemoListSpListSpinner
        spinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.itemList, android.R.layout.simple_spinner_item
        )

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spanCount = when (position) {
                    0 -> 2
                    1 -> 3
                    else -> 2
                }
                binding.MemoListRvMemoList.layoutManager = GridLayoutManager(this@MeMoListActivity, spanCount)
            }
        }
    }
}