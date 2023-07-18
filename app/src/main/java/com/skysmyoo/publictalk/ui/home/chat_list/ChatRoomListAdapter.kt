package com.skysmyoo.publictalk.ui.home.chat_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.databinding.ItemChatRoomBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import com.skysmyoo.publictalk.utils.TimeUtil

class ChatRoomListAdapter(private val viewModel: HomeViewModel) :
    ListAdapter<ChatRoom, ChatRoomListAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder.from(parent, viewModel)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatRoomViewHolder(
        private val binding: ItemChatRoomBinding,
        private val viewModel: HomeViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatRoom) {
            val messageList = item.messages.values.toList()
            val otherUser = viewModel.getOtherUser(item)
            val myEmail = viewModel.getMyEmail()
            with(binding) {
                chatRoom = item
                this.other = otherUser
                lastMessage = messageList.lastOrNull()
                unreadMessage =
                    messageList.filter {
                        !it.reading && it.receiver == myEmail
                    }.size
                time = TimeUtil.convertDateTime(messageList.lastOrNull()?.createdAt ?: "")
            }
            binding.viewModel = viewModel
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: HomeViewModel): ChatRoomViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ChatRoomViewHolder(
                    ItemChatRoomBinding.inflate(inflater, parent, false),
                    viewModel
                )
            }
        }

    }
}

class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem.member == newItem.member
    }

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem == newItem
    }
}