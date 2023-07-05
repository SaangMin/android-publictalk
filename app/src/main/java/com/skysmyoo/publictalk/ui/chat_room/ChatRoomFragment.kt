package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.databinding.FragmentChatRoomBinding
import com.skysmyoo.publictalk.utils.EventObserver

class ChatRoomFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentChatRoomBinding
    override val layoutId: Int get() = R.layout.fragment_chat_room

    private val viewModel: ChatRoomViewModel by viewModels()
    private lateinit var adapter: ChatRoomAdapter
    private val args: ChatRoomFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatRoomInfo = args.chatRoom

        adapter = ChatRoomAdapter(viewModel)
        setLayout(chatRoomInfo)
        viewModel.setAdapterItemList(chatRoomInfo.messages.values.toList())
        messageListObserver()
        newMessageObserver()

        binding.btnChatRoomSend.setOnClickListener {
            onSendMessage(chatRoomInfo)
        }
    }

    private fun setLayout(chatRoomInfo: ChatRoom) {
        val chatMember = chatRoomInfo.member
        val myEmail = viewModel.getMyEmail()
        with(binding) {
            rvChatRoom.adapter = adapter
            abChatRoom.title = chatMember.find { it == myEmail }
            abChatRoom.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun messageListObserver() {
        viewModel.adapterItemList.observe(viewLifecycleOwner, EventObserver {
            adapter.submitList(it)
        })
    }

    private fun newMessageObserver() {
        viewModel.newMessage.observe(viewLifecycleOwner, EventObserver {
            val myEmail = viewModel.getMyEmail()
            val currentList = adapter.currentList.toMutableList()
            if (it.sender == myEmail) {
                val newMessageBox = MessageBox.SenderMessageBox(it)
                currentList.add(newMessageBox)
                adapter.submitList(currentList)
            } else {
                val newMessageBox = MessageBox.ReceiverMessageBox(it)
                currentList.add(newMessageBox)
                adapter.submitList(currentList)
            }
            binding.etChatRoomMessage.setText("")
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    binding.rvChatRoom.layoutManager?.scrollToPosition(currentList.size - 1)
                }
            })
        })
    }

    private fun onSendMessage(chatRoomInfo: ChatRoom) {
        val messageText = binding.etChatRoomMessage.text.toString()
        viewModel.sendMessage(chatRoomInfo, messageText)
    }

    companion object {
        private const val TAG = "ChatRoomFragment"
    }
}