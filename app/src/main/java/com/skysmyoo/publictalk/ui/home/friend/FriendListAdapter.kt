package com.skysmyoo.publictalk.ui.home.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.databinding.ItemFriendListBinding
import com.skysmyoo.publictalk.databinding.ItemFriendListHeaderBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel

private const val TYPE_HEADER = 0
private const val TYPE_FRIEND = 1

class FriendListAdapter(private val viewModel: HomeViewModel) :
    ListAdapter<FriendListScreenData, RecyclerView.ViewHolder>(FriendListScreenDataDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FriendListScreenData.Header -> TYPE_HEADER
            is FriendListScreenData.Friend -> TYPE_FRIEND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder.from(parent)
            TYPE_FRIEND -> FriendViewHolder.from(parent, viewModel)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = getItem(position) as FriendListScreenData.Header
                holder.bind(header)
            }

            is FriendViewHolder -> {
                val friend = getItem(position) as FriendListScreenData.Friend
                holder.bind(friend)
            }
        }
    }


    class HeaderViewHolder(private val binding: ItemFriendListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendListScreenData.Header) {
            binding.tvItemFriendHeader.text = item.header
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(
                    ItemFriendListHeaderBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    class FriendViewHolder(
        private val binding: ItemFriendListBinding,
        private val viewModel: HomeViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendListScreenData.Friend) {
            val friend = item.friend
            binding.user = friend
            binding.viewModel = viewModel
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: HomeViewModel): FriendViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return FriendViewHolder(ItemFriendListBinding.inflate(inflater, parent, false), viewModel)
            }
        }
    }
}

class FriendListScreenDataDiffCallback : DiffUtil.ItemCallback<FriendListScreenData>() {
    override fun areItemsTheSame(
        oldItem: FriendListScreenData,
        newItem: FriendListScreenData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: FriendListScreenData,
        newItem: FriendListScreenData
    ): Boolean {
        return oldItem == newItem
    }

}
