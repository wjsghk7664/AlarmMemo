package com.example.alarmmemo.presentation.memoList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.FragmentListBinding
import com.example.alarmmemo.presentation.memo.MemoActivity

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MenuListAdapter
    private var sampleData = mutableListOf<ListItem>()

    private val memoActivityLaunch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val memoTitle = data?.getStringExtra("memo_title") ?: ""

            if (data != null) {
                val newMemo = ListItem(memoTitle, "2024.01.01", R.mipmap.ic_launcher)
                sampleData.add(newMemo)
                adapter.submitList(sampleData.toList())
            }
        }
    }

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

        sampleData = mutableListOf(
            ListItem("데이터 1", "date1", R.mipmap.ic_launcher)
        )

        adapter.submitList(sampleData.toList())

        binding.button.setOnClickListener {
            val intent = Intent(requireContext(), MemoActivity::class.java)
            memoActivityLaunch.launch(intent)
        }

        val listSpinner: Spinner = binding.MemoListSpListSpinner
        listSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.itemList, android.R.layout.simple_spinner_item
        )

        listSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                binding.MemoListRvMemoList.layoutManager =
                    GridLayoutManager(requireContext(), spanCount)
            }
        }

        val shuffledSpinner: Spinner = binding.MemoListSpShuffledSpinner
        shuffledSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.shuffled_itemList, android.R.layout.simple_spinner_item
        )

        shuffledSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> sampleData.sortBy { it.title }
                    1 -> sampleData.sortByDescending { it.title }
                }

                adapter.submitList(sampleData.toList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}