package com.example.alarmmemo

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
import com.example.alarmmemo.databinding.ActivityMemoListBinding

class MeMoListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMemoListBinding.inflate(layoutInflater) }
    private lateinit var adapter: MenuListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinner: Spinner = binding.MemoListSpListShuffled
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
            }
        }

        adapter = MenuListAdapter()
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
    }
}