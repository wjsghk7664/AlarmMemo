package com.team5.alarmmemo.presentation.memoList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.team5.alarmmemo.R
import androidx.fragment.app.activityViewModels
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.databinding.FragmentMemoListBinding
import com.team5.alarmmemo.presentation.memo.MemoActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class MemoListFragment : Fragment() {

    private var _binding: FragmentMemoListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MemoListAdapter
    private var check = false
    private val listViewModel: ListViewModel by activityViewModels()
    private var sort: String = SORT_BY_TIME

    companion object {
        const val SORT_BY_TIME = "시간순"
        const val SORT_BY_TITLE = "제목순"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = requireActivity().intent.getParcelableExtra<User>("user")?:User("default")
        val id = user.email
        listViewModel.setId(id)


        // 저장된 리스트를 불러옴
        listViewModel.loadList()

        // Adapter랑 연결
        adapter = MemoListAdapter(
            // 아이템 (썸네일) 클릭 시 메모 Activity로 이동
            onItemClicked = { item ->
                val intent = Intent(requireContext(), MemoActivity::class.java).apply {
                    putExtra("isLocal",true)
                    putExtra("isInit", false)
                    putExtra("userId",id)
                    putExtra("uniqueId",item.first)
                }
                startActivity(intent)
            },

            // 길게 클릭 시 아이템 삭제 다이얼로그 실행
            onItemLongClicked = { item ->
                val builder = AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.memoList_dialog_title))
                    setMessage(getString(R.string.memoList_dialog_message))

                    setPositiveButton("삭제") { _, _ ->
                        listViewModel.deleteItem(item)
                    }

                    setNegativeButton("취소") { dialog, _ ->
                        dialog.dismiss()
                    }
                }

                builder.show()
            }
        )

        // SpanCount 관련 LiveData로 업데이트
        listViewModel.spanCount.observe(viewLifecycleOwner) { spanCount ->
            binding.memoListRvMemoList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        }

        // 아이템 데이터 업데이트
        listViewModel.sampleData.observe(viewLifecycleOwner) { sampleData ->
            val sortList = when (sort) {
                SORT_BY_TIME -> sampleData.sortedByDescending { item ->
                    item.first
                }
                SORT_BY_TITLE -> sampleData.sortedBy { it.second }
                else -> sampleData
            }

            adapter.submitList(sortList)
        }

        with(binding) {
            memoListRvMemoList.adapter = adapter

            // 아이템 추가 버튼 클릭 시 아이템 추가
            memoListBtnAddButton.setOnClickListener {
                val uniqId =System.currentTimeMillis().toString()
                val addList = listViewModel.sampleData.value ?: listOf()
                val sortedList = when (sort) {
                    SORT_BY_TIME -> addList.sortedByDescending { item ->
                    item.first
                    //SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(item.date)
                    }
                    SORT_BY_TITLE -> addList.sortedBy { it.second }
                    else -> addList
                }
                adapter.submitList(sortedList.toMutableList())
                Log.d("리스트",sortedList.toString())

                val intent = Intent(requireContext(), MemoActivity::class.java).apply {
                    putExtra("isLocal",true)
                    putExtra("isInit", true)
                    putExtra("userId",id)
                    putExtra("uniqueId",uniqId)
                }
                startActivity(intent)
            }

            // Spinner 클릭시 Motion Layout 적용
            memoListTvSpinner.setOnClickListener {
                if (check) {
                    binding.motionLayout.transitionToStart()
                } else {
                    binding.motionLayout.transitionToEnd()
                }
                check = !check
            }

            // Spinner에서 시간 순 텍스트 클릭 시 시간(최신 -> 과거) 순으로 리스트 아이템 정렬
            memoListTvSortTime.setOnClickListener {
                sort = SORT_BY_TIME
                "시간순".also { binding.memoListTvSpinner.text = it }
                val currentList = listViewModel.sampleData.value ?: listOf()
                val sortedList = currentList.sortedByDescending { item ->
                    item.first
                }
                adapter.submitList(sortedList)
                memoListTvSpinner.callOnClick()
            }

            // Spinner에서 제목 순 텍스트 클릭 시 제목(ㄱ -> ㅎ) 순으로 리스트 아이템 정렬
            memoListTvSortTitle.setOnClickListener {
                sort = SORT_BY_TITLE
                "제목순".also { binding.memoListTvSpinner.text = it }
                val currentList = listViewModel.sampleData.value ?: listOf()
                val sortedList = currentList.sortedBy { it.second }
                adapter.submitList(sortedList)
                memoListTvSpinner.callOnClick()
            }

            memoListIvFilterButton.setOnClickListener {
                val bottomSheet = BottomSheetFragment { newSpanCount ->
                    listViewModel.setSpanCount(newSpanCount)
                }
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        listViewModel.loadList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
