package com.example.alarmmemo.presentation.memoList

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmmemo.databinding.ListSampleBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class MemoListAdapter(
    private val onItemClicked: (ListItem) -> Unit,
) : ListAdapter<ListItem, ListItemViewHolder>(object : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListSampleBinding.inflate(layoutInflater, parent, false)
        return ListItemViewHolder(binding, onItemClicked, this)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ListItemViewHolder(
    private val binding: ListSampleBinding,
    private val onItemClicked: (ListItem) -> Unit,
    private val adapter: MemoListAdapter
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ListItem) {
        with(binding) {
            SampleTvMemoTitle.text = item.title
            SampleTvMemoDate.text = item.date
            SampleTvMemoThumbnail.setImageResource(item.image)

            SampleTvMemoDate.setOnClickListener {
                showDatePickerDialog(item)
            }

            SampleTvMemoThumbnail.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    private fun showDatePickerDialog(item: ListItem) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val formattedDate = dateFormat.format(selected)

            val updatedList = adapter.currentList.toMutableList().apply {
                val index = indexOfFirst { it.number == item.number }
                if (index != -1) set(index, item.copy(date = formattedDate))
            }

            adapter.submitList(updatedList)
        }

        val datePickerDialog = DatePickerDialog(binding.root.context, listener, year, month, day)
        datePickerDialog.show()
    }
}