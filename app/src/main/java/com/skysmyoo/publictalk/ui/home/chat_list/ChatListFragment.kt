package com.skysmyoo.publictalk.ui.home.chat_list

import android.os.Bundle
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentChatListBinding


class ChatListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list

    private val adapter = ChatRoomListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChatList.adapter = adapter

    }
}