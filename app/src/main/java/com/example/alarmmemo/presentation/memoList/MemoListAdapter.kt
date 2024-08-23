package com.example.alarmmemo.presentation.memoList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmmemo.databinding.ListSampleBinding

class MenuListAdapter(private val onItemClicked : (ListItem) -> Unit) : ListAdapter<ListItem, ListItemViewHolder>(object : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListSampleBinding.inflate(layoutInflater, parent, false)
        return ListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }
}

class ListItemViewHolder(private val binding: ListSampleBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ListItem, onItemClicked: (ListItem) -> Unit) {
        with(binding) {
            SampleTvMemoTitle.text = item.title
            SampleTvMemoDate.text = item.date
            SampleTvMemoThumbnail.setImageResource(item.image)

            SampleTvMemoThumbnail.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}