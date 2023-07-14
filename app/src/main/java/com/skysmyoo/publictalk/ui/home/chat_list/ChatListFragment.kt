package com.skysmyoo.publictalk.ui.home.chat_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentChatListBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        setChatListUiState()
    }

    private fun setChatListUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.chatListUiState.collect {
                        if (it.isChatRoomClicked) {
                            navigateChatRoom()
                        }
                    }
                }
                launch {
                    viewModel.chatRoomList.collect {
                        adapter.submitList(it)
                    }
                }
            }
        }
    }

    private fun navigateChatRoom() {
        val clickedChatRoom = viewModel.clickedChatRoom
        if (clickedChatRoom != null) {
            val action = ChatListFragmentDirections.actionChatListToChatRoom(clickedChatRoom)
            findNavController().navigate(action)
        }
    }

    companion object {
        private const val TAG = "ChatListFragment"
    }
}