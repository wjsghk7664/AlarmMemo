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
            memoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
            memoListRvMemoList.adapter = adapter
        }

        listViewModel.sampleData.observe(viewLifecycleOwner){ sampleData ->
            adapter.submitList(sampleData)
        }

        with(binding) {
            memoListBtnAddButton.setOnClickListener {
                listViewModel.addSampleItem()
            }

            memoListTvSpinner.setOnClickListener {
//                showDropDownMenu()
            }

            memoListTvSortTime.setOnClickListener {
//            adapter.submitList(sampleData)
                "시간순".also { binding.memoListTvSpinner.text = it }
                memoListTvSpinner.callOnClick()
            }

            memoListTvSortTitle.setOnClickListener {
//            adapter.submitList(sampleData)
                "제목순".also { binding.memoListTvSpinner.text = it }
                memoListTvSpinner.callOnClick()
            }

            memoListIvDropDownButton.setOnClickListener {
                val currentList = listViewModel.sampleData.value ?: listOf()
                if (!check) {
                    val sortedList = currentList.sortedByDescending { it.number }
                    adapter.submitList(sortedList)
                    binding.memoListIvDropDownButton.setImageResource(R.drawable.ic_arrow_drop_down)
                    check = true
                } else {
                    val sortedList = currentList.sortedBy { it.number }
                    adapter.submitList(sortedList)
                    binding.memoListIvDropDownButton.setImageResource(R.drawable.ic_arrow_drop_up)
                    check = false
                }
            }
        }
    }

//    private fun showDropDownMenu() {
//        if (check) {
//            with(binding) {
//                sortLatest.visibility = View.GONE
//                sortOldest.visibility = View.GONE
//                motionLayout.transitionToEnd()
//                tvSpinner.setBackgroundColor(Color.TRANSPARENT)
//            }
//            check = false
//        } else {
//            with(binding) {
//                sortLatest.visibility = View.VISIBLE
//                sortOldest.visibility = View.VISIBLE
//                motionLayout.transitionToEnd()
//                tvSpinner.setBackgroundColor(Color.WHITE)
//            }
//            check = true
//        }
//    }

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