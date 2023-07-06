package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var chatRoomInfo: ChatRoom

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRoomInfo = args.chatRoom

        adapter = ChatRoomAdapter(viewModel)
        setLayout()
        viewModel.setAdapterItemList(chatRoomInfo.messages.values.toList())
        viewModel.getRoomKey(chatRoomInfo)
        messageListObserver()
        newMessageObserver()
        friendDataObserver()
        roomKeyObserver()

        binding.btnChatRoomSend.setOnClickListener {
            onSendMessage()
        }
    }

    override fun onPause() {
        super.onPause()
        leaveChatting()
    }

    private fun setLayout() {
        val chatMember = chatRoomInfo.member
        val myEmail = viewModel.getMyEmail()
        val otherUserEmail = chatMember.map { it.userEmail }.find { it != myEmail } ?: ""
        viewModel.findFriend(otherUserEmail)
        with(binding) {
            rvChatRoom.adapter = adapter
            abChatRoom.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
        binding.viewModel = viewModel

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvChatRoom.layoutManager?.scrollToPosition(adapter.currentList.size - 1)
            }
        })
    }

    private fun roomKeyObserver() {
        viewModel.chatRoomKey.observe(viewLifecycleOwner) {
            val myEmail = viewModel.getMyEmail()
            val myIdKey = chatRoomInfo.member.indexOfFirst { member -> member.userEmail == myEmail }
            val chatRoomRef =
                FirebaseDatabase.getInstance().getReference("chatRooms/$it/member/$myIdKey")
            chatRoomRef.child("isChatting").setValue(true)
            viewModel.listenForChat(it)
        }
    }

    private fun leaveChatting() {
        val roomKey = viewModel.chatRoomKey.value
        val myEmail = viewModel.getMyEmail()
        val myIdKey = chatRoomInfo.member.indexOfFirst { member -> member.userEmail == myEmail }
        val chatRoomRef =
            FirebaseDatabase.getInstance().getReference("chatRooms/$roomKey/member/$myIdKey")
        chatRoomRef.child("isChatting").setValue(false)
    }

    private fun friendDataObserver() {
        viewModel.friendData.observe(viewLifecycleOwner, EventObserver {
            binding.abChatRoom.title = it.userName
        })
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
        })
    }

    private fun onSendMessage() {
        viewModel.sendMessage(chatRoomInfo)
    }

    companion object {
        private const val TAG = "ChatRoomFragment"
    }
}