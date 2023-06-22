package com.skysmyoo.publictalk.ui.home.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.databinding.ItemFriendListBinding
import com.skysmyoo.publictalk.databinding.ItemFriendListHeaderBinding

private const val TYPE_HEADER = 0
private const val TYPE_FRIEND = 1

class FriendListAdapter :
    ListAdapter<FriendListScreenData, RecyclerView.ViewHolder>(FriendListScreenDataDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FriendListScreenData.Header -> TYPE_HEADER
            is FriendListScreenData.FriendListItem -> TYPE_FRIEND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder.from(parent)
            TYPE_FRIEND -> FriendListViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = getItem(position) as FriendListScreenData.Header
                holder.bind(header)
            }

            is FriendListViewHolder -> {
                val friend = getItemId(position) as FriendListScreenData.FriendListItem
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

    class FriendListViewHolder(
        private val binding: ItemFriendListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendListScreenData.FriendListItem) {
            val friend = item.friend
            binding.ivItemFriendProfile.load(friend.userProfileImage)
            binding.tvItemFriendName.text = friend.userName
        }

        companion object {
            fun from(parent: ViewGroup): FriendListViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return FriendListViewHolder(ItemFriendListBinding.inflate(inflater, parent, false))
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
