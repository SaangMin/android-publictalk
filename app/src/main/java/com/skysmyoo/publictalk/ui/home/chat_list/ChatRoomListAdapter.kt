package com.skysmyoo.publictalk.ui.home.chat_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.databinding.ItemChatRoomBinding

class ChatRoomListAdapter :
    ListAdapter<ChatRoom, ChatRoomListAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatRoomViewHolder(private val binding: ItemChatRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatRoom) {
            with(binding) {
                chatRoom = item
                lastMessage = item.messageList?.lastOrNull()
                unreadMessage =
                    item.messageList?.filter {
                        !it.isReading && it.receiver == preferencesManager.getMyEmail()
                    }?.size ?: 0
            }
        }

        companion object {
            fun from(parent: ViewGroup): ChatRoomViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ChatRoomViewHolder(
                    ItemChatRoomBinding.inflate(inflater, parent, false)
                )
            }
        }

    }
}

class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem == newItem
    }
}