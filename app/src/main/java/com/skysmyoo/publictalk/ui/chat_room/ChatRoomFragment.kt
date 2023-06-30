package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatRoomInfo = args.chatRoom

        adapter = ChatRoomAdapter(viewModel)
        setLayout(chatRoomInfo)
        viewModel.setAdapterItemList(chatRoomInfo.messageList ?: emptyList())
        messageListObserver()

    }

    private fun setLayout(chatRoomInfo: ChatRoom) {
        with(binding) {
            rvChatRoom.adapter = adapter
            abChatRoom.title = chatRoomInfo.other.userName
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

    companion object {
        private const val TAG = "ChatRoomFragment"
    }
}