package com.dicoding.picodiploma.submission_story_app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.submission_story_app.data.helper.DiffCallBack
import com.dicoding.picodiploma.submission_story_app.data.response.ListStoryItem
import com.dicoding.picodiploma.submission_story_app.databinding.StoryItemBinding


class StoryAdapter: PagingDataAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback){
        this.onItemClickCallback = onItemClickCallback
    }


    inner class StoryViewHolder(private val v: StoryItemBinding): RecyclerView.ViewHolder(v.root){
        fun bind(item: ListStoryItem){
            v.root.setOnClickListener {
                onItemClickCallback?.onItemClicked(item)
            }
            Glide.with(v.imgPost)
                .load(item.photoUrl)
                .into(v.imgPost)
            v.tvUserName.text = item.name
            v.tvCaption.text = item.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder =
        StoryViewHolder(StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    interface OnItemClickCallback{
        fun onItemClicked(data: ListStoryItem)
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ListStoryItem>() {
                override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean =
                    oldItem == newItem

                override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean =
                    oldItem.id == newItem.id
            }
    }

}
