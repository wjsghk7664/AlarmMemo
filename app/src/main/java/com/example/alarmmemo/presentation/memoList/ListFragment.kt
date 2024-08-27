package com.example.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.FragmentListBinding
import com.example.alarmmemo.presentation.memo.MemoActivity

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
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

        adapter = MenuListAdapter { listItem ->
            val intent = Intent(requireContext(), MemoActivity::class.java)
            startActivity(intent)
        }

        val spanCount = arguments?.getInt("spanCount") ?: 2
        binding.MemoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.MemoListRvMemoList.adapter = adapter

        val sampleData = listOf(
            ListItem("메모 1", "2024-08-27", R.mipmap.ic_launcher),
            ListItem("메모 2", "2024-08-26", R.mipmap.ic_launcher),
            ListItem("메모 3", "2024-08-27", R.mipmap.ic_launcher),
            ListItem("메모 3", "2024-08-27", R.mipmap.ic_launcher)
        )

        adapter.submitList(sampleData)

        val sortedSpinner: Spinner = binding.MemoListSpSortedSpinner
        sortedSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.sorted_itemList, android.R.layout.simple_spinner_item
        )

        sortedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> sampleData.sortedBy { it.title }
                    1 -> sampleData.sortedByDescending { it.title }
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