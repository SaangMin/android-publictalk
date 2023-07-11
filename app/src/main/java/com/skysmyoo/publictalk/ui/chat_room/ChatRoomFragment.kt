package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
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

    override fun onStop() {
        super.onStop()
        viewModel.setIsNotChatting(chatRoomInfo)
    }

    private fun setLayout() {
        viewModel.findFriend(chatRoomInfo)
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
            enterChatting()
            viewModel.listenForChat(chatRoomInfo, it)
        }
    }

    private fun enterChatting() {
        viewModel.enterChatting(chatRoomInfo)
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
            val currentList = adapter.currentList.toMutableList()
            currentList.add(it)
            adapter.submitList(currentList)
        })
    }

    private fun onSendMessage() {
        viewModel.sendMessage(chatRoomInfo) {
            Toast.makeText(
                requireContext(),
                getString(R.string.empty_message_error),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.etChatRoomMessage.setText("")
    }

    companion object {
        private const val TAG = "ChatRoomFragment"
    }
}