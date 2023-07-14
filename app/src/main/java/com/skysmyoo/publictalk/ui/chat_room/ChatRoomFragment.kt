package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.databinding.FragmentChatRoomBinding
import kotlinx.coroutines.launch

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
        viewModel.getRoomKey(chatRoomInfo)
        setChatRoomUiState()
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

    private fun setChatRoomUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.chatRoomUiState.collect {
                        if (it.isGetChatRoomKey) {
                            viewModel.enterChatting(chatRoomInfo)
                            viewModel.messageListener(chatRoomInfo)
                        }
                        if (it.otherUser != null) {
                            binding.abChatRoom.title = it.otherUser.userName
                        }
                        if (it.isFirebaseError) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.firebase_error_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        if (it.isNetworkError) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.network_error_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                launch {
                    viewModel.adapterItemList.collect {
                        adapter.submitList(it)
                    }
                }
            }
        }
    }

    private fun onSendMessage() {
        viewModel.sendMessage(chatRoomInfo) {
            Toast.makeText(
                requireContext(),
                getString(R.string.empty_message_error),
                Toast.LENGTH_SHORT
            ).show()
        }
        val body = binding.etChatRoomMessage.text.toString()
        if (body.isNotEmpty()) {
            val action = ChatRoomFragmentDirections.actionChatRoomToTranslate(body)
            findNavController().navigate(action)
        }
        binding.etChatRoomMessage.setText("")
    }

    companion object {
        private const val TAG = "ChatRoomFragment"
    }
}