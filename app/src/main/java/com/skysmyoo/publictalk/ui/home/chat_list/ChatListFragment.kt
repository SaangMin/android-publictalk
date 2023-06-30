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
            "217381293791"
        )
        val sampleChatRoom = ChatRoom(
            0,
            viewModel.getMyEmail(),
            TestSampleData.sampleUser,
            listOf(sampleMessage, sampleMessage),
            "148290321"
        )

        adapter.submitList(listOf(sampleChatRoom))
    }

    private fun chatRoomClickObserver() {
        viewModel.chatRoomClickEvent.observe(viewLifecycleOwner) {
            val clickedChatRoom = viewModel.clickedChatRoom
            if(clickedChatRoom != null) {
                val action = ChatListFragmentDirections.actionChatListToChatRoom(clickedChatRoom)
                findNavController().navigate(action)
            }
        }
    }
}