package com.team5.alarmmemo.presentation.memoList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.team5.alarmmemo.databinding.ListSample2Binding

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
        val binding = ListSample2Binding.inflate(layoutInflater, parent, false)
        return ListItemViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ListItemViewHolder(
    private val binding: ListSample2Binding,
    private val onItemClicked: (ListItem) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ListItem) {
        with(binding) {
            sampleTvMemoTitle.text = item.title
            sampleTvMemoDate.text = item.date
            sampleIvMemoThumbnail.setImageResource(item.image)

            sampleIvMemoThumbnail.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}
