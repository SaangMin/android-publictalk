package com.skysmyoo.publictalk.ui.home.chat_list

import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentChatListBinding
import com.skysmyoo.publictalk.receiver.MyBroadcastReceiver
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import com.skysmyoo.publictalk.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ChatRoomListAdapter
    private val receiver = MyBroadcastReceiver()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().registerReceiver(receiver, IntentFilter(Constants.REFRESH_CHAT_ROOM_LIST))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatRoomListAdapter(viewModel)
        binding.rvChatList.adapter = adapter
        setChatListUiState()
        viewModel.refreshChatRoomList()
        receiver.setOnChatRoomListUpdate {
            viewModel.refreshChatRoomList()
        }
    }

    private fun setChatListUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.chatListUiState.collect {
                        if (it.isChatRoomClicked) {
                            navigateChatRoom()
                        }
                        if (it.isChatListUpdated) {
                            adapter.submitList(viewModel.chatRoomList.value.sortedByDescending { chatRoom -> chatRoom.messages.values.lastOrNull()?.createdAt ?: chatRoom.chatCreatedAt })
                        }
                    }
                }
            }
        }
    }

    private fun navigateChatRoom() {
        val clickedChatRoom = viewModel.clickedChatRoom
        if (clickedChatRoom != null && findNavController().currentDestination?.id == R.id.navigation_chat_list) {
            val action = ChatListFragmentDirections.actionChatListToChatRoom(clickedChatRoom)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver)
    }
}