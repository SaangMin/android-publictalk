package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.View
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
import com.skysmyoo.publictalk.utils.isNetworkAvailable
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
        viewModel.messageListener(chatRoomInfo)
        setLayout()
        viewModel.getRoomKey(chatRoomInfo)
        setChatRoomUiState()
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
            abChatRoom.setOnMenuItemClickListener {
                if (isNetworkAvailable(requireContext())) {
                    val action = ChatRoomFragmentDirections.actionChatRoomToRemoveChat(chatRoomInfo)
                    findNavController().navigate(action)
                    true
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.network_error_msg),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    true
                }
            }
        }
        binding.btnChatRoomSend.setOnClickListener {
            if (isNetworkAvailable(requireContext())) {
                viewModel.startTranslate(chatRoomInfo)
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.network_error_msg),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        binding.viewModel = viewModel
        binding.chatRoom = chatRoomInfo

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
                        }
                        if (it.otherUser != null) {
                            binding.abChatRoom.title = it.otherUser.userName
                        } else {
                            val otherEmail = chatRoomInfo.member.map { member -> member.userEmail }
                                .find { email -> email != viewModel.getMyEmail() }
                            binding.abChatRoom.title = otherEmail
                        }
                        if (it.isNetworkError) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.network_error_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        if (it.isNotFoundUser) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.other_user_not_friend_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        if (it.isSendBtnClicked) {
                            binding.etChatRoomMessage.setText("")
                        }
                    }
                }
                launch {
                    viewModel.adapterItemList.collect {
                        adapter.submitList(it)
                    }
                }
                launch {
                    viewModel.isEmptyMessage.collect {
                        if (it) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.empty_message_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                launch {
                    viewModel.isTranslated.collect {
                        if (it) {
                            val translatedBody = viewModel.translatedText
                            val action =
                                ChatRoomFragmentDirections.actionChatRoomToTranslate(
                                    chatRoomInfo,
                                    viewModel.inputBody,
                                    translatedBody,
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
                launch {
                    viewModel.isFirebaseError.collect {
                        if (it) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.firebase_error_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}