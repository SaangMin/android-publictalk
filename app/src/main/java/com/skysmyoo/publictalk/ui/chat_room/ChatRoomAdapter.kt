package com.skysmyoo.publictalk.ui.chat_room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.databinding.ItemReceiverMessageBoxBinding
import com.skysmyoo.publictalk.databinding.ItemSenderMessageBoxBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import javax.inject.Inject

private const val TYPE_SENDER = 0
private const val TYPE_RECEIVER = 1

class ChatRoomAdapter @Inject constructor(
    private val viewModel: HomeViewModel
) : ListAdapter<MessageBox, RecyclerView.ViewHolder>(MessageBoxDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).id.sender) {
            viewModel.getMyEmail() -> TYPE_SENDER
            else -> TYPE_RECEIVER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENDER -> SenderViewHolder.from(parent)
            TYPE_RECEIVER -> ReceiverViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SenderViewHolder -> {
                val message = getItem(position) as MessageBox.SenderMessageBox
                holder.bind(message)
            }

            is ReceiverViewHolder -> {
                val message = getItem(position) as MessageBox.ReceiverMessageBox
                holder.bind(message)
            }
        }
    }

    class SenderViewHolder(private val binding: ItemSenderMessageBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessageBox.SenderMessageBox) {
            val message = item.message
            binding.message = message
        }

        companion object {
            fun from(parent: ViewGroup): SenderViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return SenderViewHolder(
                    ItemSenderMessageBoxBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    class ReceiverViewHolder(private val binding: ItemReceiverMessageBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessageBox.ReceiverMessageBox) {
            val message = item.message
            binding.message = message
        }

        companion object {
            fun from(parent: ViewGroup): ReceiverViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ReceiverViewHolder(
                    ItemReceiverMessageBoxBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }
}

class MessageBoxDiffCallback : DiffUtil.ItemCallback<MessageBox>() {
    override fun areItemsTheSame(oldItem: MessageBox, newItem: MessageBox): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageBox, newItem: MessageBox): Boolean {
        return oldItem == newItem
    }

}
