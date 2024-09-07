package com.team5.alarmmemo.presentation.memoList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.team5.alarmmemo.R
import androidx.fragment.app.activityViewModels
import com.team5.alarmmemo.databinding.FragmentListBinding
import com.team5.alarmmemo.presentation.memo.MemoActivity
import java.text.SimpleDateFormat
import java.util.Locale

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MemoListAdapter
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

        // 저장된 리스트를 불러옴
        listViewModel.loadList()

        // Adapter랑 연결
        adapter = MemoListAdapter(
            // 아이템 (썸네일) 클릭 시 메모 Activity로 이동
            onItemClicked = { _ ->
                val intent = Intent(requireContext(), MemoActivity::class.java)
                startActivity(intent)
            },

            // 길게 클릭 시 아이템 삭제 다이얼로그 실행
            onItemLongClicked = { item ->
                val builder = AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.memoList_dialog_title))
                    setMessage(getString(R.string.memoList_dialog_message))

                    setPositiveButton("삭제") { _, _ ->
                        listViewModel.deleteItem(item)
                        Toast.makeText(requireContext(), R.string.memoList_delete_message, Toast.LENGTH_SHORT).show()
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
            adapter.submitList(sampleData)
        }

        with(binding) {
            memoListRvMemoList.adapter = adapter

            // 아이템 추가 버튼 클릭 시 아이템 추가
            memoListBtnAddButton.setOnClickListener {
                listViewModel.addSampleItem()
                val addList = listViewModel.sampleData.value ?: listOf()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                val sortedList = addList.sortedByDescending { item ->
                    dateFormat.parse(item.date)
                }

                adapter.submitList(sortedList)

                Toast.makeText(requireContext(), R.string.memoList_add_message, Toast.LENGTH_SHORT).show()
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
                "시간순".also { binding.memoListTvSpinner.text = it }
                val currentList = listViewModel.sampleData.value ?: listOf()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val sortedList = currentList.sortedByDescending { item ->
                    dateFormat.parse(item.date)
                }
                adapter.submitList(sortedList)
                memoListTvSpinner.callOnClick()
            }

            // Spinner에서 제목 순 텍스트 클릭 시 제목(ㄱ -> ㅎ) 순으로 리스트 아이템 정렬
            memoListTvSortTitle.setOnClickListener {
                "제목순".also { binding.memoListTvSpinner.text = it }
                val currentList = listViewModel.sampleData.value ?: listOf()
                val sortedList = currentList.sortedBy { it.title }
                adapter.submitList(sortedList)
                memoListTvSpinner.callOnClick()
            }

//            memoListIvDropDownButton.setOnClickListener {
//                if (!check) {
//                    binding.memoListIvDropDownButton.setImageResource(R.drawable.ic_arrow_drop_down)
//                } else {
//                    binding.memoListIvDropDownButton.setImageResource(R.drawable.ic_arrow_drop_up)
//                }
//                check = !check
//            }

            // filter 버튼 클릭 시 2줄 격자, 3줄 격자 선택하는 bottom sheet 나오게 하고 거기서 SpanCount 꺼내서 가져 오기(?)
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
