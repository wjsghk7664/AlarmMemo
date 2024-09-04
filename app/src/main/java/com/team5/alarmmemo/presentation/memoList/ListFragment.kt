package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.team5.alarmmemo.R
import androidx.fragment.app.activityViewModels
import com.team5.alarmmemo.databinding.FragmentListBinding
import com.team5.alarmmemo.presentation.memo.MemoActivity

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MemoListAdapter
    private var check = false
    private val listViewModel: ListViewModel by activityViewModels()
    private var spanCount = 2
//    private var sampleData = listOf<ListItem>()
//    private var number = 0

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

        listViewModel.spanCount.observe(viewLifecycleOwner) { spanCount ->
            binding.memoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        }

        with(binding) {
            memoListRvMemoList.adapter = adapter

            listViewModel.sampleData.observe(viewLifecycleOwner) { sampleData ->
                adapter.submitList(sampleData)
            }

            memoListBtnAddButton.setOnClickListener {
                listViewModel.addSampleItem()
            }

            memoListTvSpinner.setOnClickListener {
                if (check) {
                    binding.motionLayout.transitionToStart()
                } else {
                    binding.motionLayout.transitionToEnd()
                }
                check = !check
            }

            memoListTvSortTime.setOnClickListener {
                "시간순".also { binding.memoListTvSpinner.text = it }
                val currentList = listViewModel.sampleData.value ?: listOf()
                val sortedList = currentList.sortedBy { it.date }
                adapter.submitList(sortedList)
                memoListTvSpinner.callOnClick()
            }

            memoListTvSortTitle.setOnClickListener {
                "제목순".also { binding.memoListTvSpinner.text = it }
                val currentList = listViewModel.sampleData.value ?: listOf()
                val sortedList = currentList.sortedBy { it.title }
                adapter.submitList(sortedList)
                memoListTvSpinner.callOnClick()
            }

            memoListIvDropDownButton.setOnClickListener {
                if (!check) {
                    binding.memoListIvDropDownButton.setImageResource(R.drawable.ic_arrow_drop_down)
                } else {
                    binding.memoListIvDropDownButton.setImageResource(R.drawable.ic_arrow_drop_up)
                }
                check = !check
            }

            memoListIvFilterButton.setOnClickListener {
                val bottomSheet = BottomSheetFragment { newSpanCount ->
                    listViewModel.setSpanCount(newSpanCount)
                }
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
