package com.team5.alarmmemo.presentation.memoList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.FragmentBottomSheetBinding

class BottomSheetFragment (private val selected: (Int) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val spanCount = when (checkedId) {
                R.id.radio_button_1 -> 2
                R.id.radio_button_2 -> 3
                else -> 2
            }
            Toast.makeText(requireContext(), "${spanCount}줄 격자 선택", Toast.LENGTH_SHORT).show()

            selected(spanCount)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
