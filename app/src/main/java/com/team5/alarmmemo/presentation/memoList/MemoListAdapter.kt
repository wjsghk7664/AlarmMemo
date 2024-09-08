package com.team5.alarmmemo.presentation.memoList

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.team5.alarmmemo.databinding.ListSampleBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class MemoListAdapter(
    private val onItemClicked: (Triple<String,String, SpannableStringBuilder>) -> Unit,
    private val onItemLongClicked: (Triple<String,String, SpannableStringBuilder>) -> Unit,
) : ListAdapter<Triple<String,String, SpannableStringBuilder>, ListItemViewHolder>(object : DiffUtil.ItemCallback<Triple<String,String, SpannableStringBuilder>>() {
    override fun areItemsTheSame(oldItem: Triple<String,String, SpannableStringBuilder>, newItem: Triple<String,String, SpannableStringBuilder>): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(oldItem: Triple<String,String, SpannableStringBuilder>, newItem: Triple<String,String, SpannableStringBuilder>): Boolean {
        return oldItem == newItem
    }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListSampleBinding.inflate(layoutInflater, parent, false)
        return ListItemViewHolder(binding, onItemClicked, onItemLongClicked)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ListItemViewHolder(
    private val binding: ListSampleBinding,
    private val onItemClicked: (Triple<String,String, SpannableStringBuilder>) -> Unit,
    private val onItemLongClicked: (Triple<String,String, SpannableStringBuilder>) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Triple<String,String, SpannableStringBuilder>) {
        with(binding) {
            sampleTvMemoTitle.text = item.second
            sampleTvMemoDate.text = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.first.toLong()), ZoneId.systemDefault()).toString()
            sampleIvMemoThumbnail.text = item.third

            sampleIvMemoThumbnail.setOnClickListener {
                onItemClicked(item)
            }

            sampleIvMemoThumbnail.setOnLongClickListener {
                onItemLongClicked(item)
                true
            }
        }
    }
}
