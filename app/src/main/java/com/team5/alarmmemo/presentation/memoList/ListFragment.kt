package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.FragmentListBinding
import com.team5.alarmmemo.presentation.memo.MemoActivity

class ListFragment : Fragment() {

    private var _binding : FragmentListBinding?= null
    private val binding get() = _binding!!
    private lateinit var adapter: MenuListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MenuListAdapter {
            val intent = Intent(requireContext(), MemoActivity::class.java)
            startActivity(intent)
        }

        val spanCount = arguments?.getInt("spanCount") ?: 2

        binding.MemoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
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

        /*val spinner: Spinner = binding.MemoListSpListSpinner
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
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}