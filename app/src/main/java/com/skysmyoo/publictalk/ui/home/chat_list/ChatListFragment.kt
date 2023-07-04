package com.skysmyoo.publictalk.ui.home.chat_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.TestSampleData
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.databinding.FragmentChatListBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import com.skysmyoo.publictalk.utils.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ChatRoomListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatRoomListAdapter(viewModel)
        binding.rvChatList.adapter = adapter
        setSampleChatRoom()
        chatRoomClickObserver()
    }

    private fun setSampleChatRoom() {
        val sampleMessage = Message(
            TestSampleData.sampleUser.userEmail,
            viewModel.getMyEmail(),
            "안녕!",
            false,
            "2023-06-29 13:44:15"
        )

        val sampleMessage2 = Message(
            viewModel.getMyEmail(),
            TestSampleData.sampleUser.userEmail,
            "응 안녕!",
            false,
            "2023-06-30 13:50:24"
        )

        val sampleChatRoom = ChatRoom(
            viewModel.getMyEmail(),
            TestSampleData.sampleUser,
            mapOf(
                ("dnsj" to sampleMessage),
                ("dnsj" to sampleMessage),
                ("dnsj" to sampleMessage2)
            ),
            "148290321"
        )

        adapter.submitList(listOf(sampleChatRoom))
    }

    private fun chatRoomClickObserver() {
        viewModel.chatRoomClickEvent.observe(viewLifecycleOwner, EventObserver {
            val clickedChatRoom = viewModel.clickedChatRoom
            if (clickedChatRoom != null) {
                val action = ChatListFragmentDirections.actionChatListToChatRoom(clickedChatRoom)
                findNavController().navigate(action)
            }
        })
    }

    companion object {
        private const val TAG = "ChatListFragment"
    }
}