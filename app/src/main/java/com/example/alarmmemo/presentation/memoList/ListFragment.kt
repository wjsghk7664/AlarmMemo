package com.example.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.alarmmemo.R
import android.graphics.Color
import com.example.alarmmemo.databinding.FragmentListBinding
import com.example.alarmmemo.presentation.memo.MemoActivity

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MemoListAdapter
    private var sampleData = listOf<ListItem>()
    private var number = 0
    private var check = false

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

        adapter = MemoListAdapter(
            onItemClicked = { _ ->
                val intent = Intent(requireContext(), MemoActivity::class.java)
                startActivity(intent)
            }
        )

        val spanCount = arguments?.getInt("spanCount") ?: 2
        with(binding) {
            MemoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
            MemoListRvMemoList.adapter = adapter
        }

        adapter.submitList(sampleData)

        binding.button.setOnClickListener {
            addSampleItem()
        }

        binding.tvSpinner.setOnClickListener {
            showDropDownMenu()
        }

        binding.sortLatest.setOnClickListener {
//            adapter.submitList(sampleData)
            "시간순".also { binding.tvSpinner.text = it }
            showDropDownMenu()
        }

        binding.sortOldest.setOnClickListener {
//            adapter.submitList(sampleData)
            "제목순".also { binding.tvSpinner.text = it }
            showDropDownMenu()
        }

        binding.dropDownButton.setOnClickListener {
            if (!check) {
                sampleData = sampleData.sortedByDescending { it.number }
                adapter.submitList(sampleData)
                binding.dropDownButton.setImageResource(R.drawable.spinner_bg)
                check = true
            } else {
                sampleData = sampleData.sortedBy { it.number }
                adapter.submitList(sampleData)
                binding.dropDownButton.setImageResource(R.drawable.ic_pad)
                check = false
            }
        }
    }

    private fun showDropDownMenu() {
        if (check) {
            binding.popupMenuLayout.visibility = View.GONE
            binding.tvSpinner.setBackgroundColor(Color.TRANSPARENT)
            check = false
        } else {
            binding.popupMenuLayout.visibility = View.VISIBLE
            binding.tvSpinner.setBackgroundColor(Color.WHITE)
            check = true
        }
    }

    private fun addSampleItem() {
        val item = ListItem(
            number = number++,
            title = "새 메모",
            date = "2024-08-28",
            image = R.mipmap.ic_launcher
        )
        sampleData += item
        adapter.submitList(sampleData.toMutableList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}