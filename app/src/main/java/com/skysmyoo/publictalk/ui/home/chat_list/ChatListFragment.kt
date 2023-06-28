package com.skysmyoo.publictalk.ui.home.chat_list

import android.os.Bundle
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.TestSampleData
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.databinding.FragmentChatListBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list
    private val adapter = ChatRoomListAdapter()
    @Inject
    lateinit var repository: UserRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChatList.adapter = adapter
        setSampleChatRoom()
    }

    private fun setSampleChatRoom() {
        val sampleMessage = Message(
            TestSampleData.sampleUser.userEmail,
            repository.getMyEmail() ?: "",
            "안녕!",
            false,
            "217381293791"
        )
        val sampleChatRoom = ChatRoom(
            0,
            repository.getMyEmail() ?: "",
            TestSampleData.sampleUser,
            listOf(sampleMessage, sampleMessage),
            "148290321"
        )

        adapter.submitList(listOf(sampleChatRoom))
    }
}