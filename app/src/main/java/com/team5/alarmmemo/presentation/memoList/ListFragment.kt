package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.team5.alarmmemo.R
import android.graphics.Color
import android.util.Log
import androidx.fragment.app.activityViewModels
import com.team5.alarmmemo.databinding.FragmentListBinding
import com.team5.alarmmemo.presentation.memo.MemoActivity

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MemoListAdapter
//    private var sampleData = listOf<ListItem>()
//    private var number = 0
    private var check = false
    private val listViewModel: ListViewModel by activityViewModels()

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
        Log.d("ListFragment", spanCount.toString())
        with(binding) {
            MemoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
            MemoListRvMemoList.adapter = adapter
        }

        listViewModel.sampleData.observe(viewLifecycleOwner){ sampleData ->
            adapter.submitList(sampleData)
        }

        with(binding) {
            button.setOnClickListener {
                listViewModel.addSampleItem()
            }

            tvSpinner.setOnClickListener {
//                showDropDownMenu()
            }

            sortLatest.setOnClickListener {
//            adapter.submitList(sampleData)
                "시간순".also { binding.tvSpinner.text = it }
                tvSpinner.callOnClick()
            }

            sortOldest.setOnClickListener {
//            adapter.submitList(sampleData)
                "제목순".also { binding.tvSpinner.text = it }
                tvSpinner.callOnClick()
            }

            dropDownButton.setOnClickListener {
                val currentList = listViewModel.sampleData.value ?: listOf()
                if (!check) {
                    val sortedList = currentList.sortedByDescending { it.number }
                    adapter.submitList(sortedList)
                    binding.dropDownButton.setImageResource(R.drawable.spinner_bg)
                    check = true
                } else {
                    val sortedList = currentList.sortedBy { it.number }
                    adapter.submitList(sortedList)
                    binding.dropDownButton.setImageResource(R.drawable.ic_pad)
                    check = false
                }
            }
        }
    }

    private fun showDropDownMenu() {
        if (check) {
            with(binding) {
//                sortLatest.visibility = View.GONE
//                sortOldest.visibility = View.GONE
                motionLayout.transitionToEnd()
                tvSpinner.setBackgroundColor(Color.TRANSPARENT)
            }
            check = false
        } else {
            with(binding) {
//                sortLatest.visibility = View.VISIBLE
//                sortOldest.visibility = View.VISIBLE
                motionLayout.transitionToEnd()
                tvSpinner.setBackgroundColor(Color.WHITE)
            }
            check = true
        }
    }

//    private fun addSampleItem() {
//        val item = ListItem(
//            number = number++,
//            title = "새 메모",
//            date = "2024-08-28",
//            image = R.mipmap.ic_launcher
//        )
//        sampleData += item
//        adapter.submitList(sampleData.toMutableList())
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}