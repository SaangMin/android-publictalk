package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.databinding.DialogFriendInfoBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendInfoDialogFragment : DialogFragment() {

    private var _binding: DialogFriendInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val args: FriendInfoDialogFragmentArgs by navArgs()
    private lateinit var friendInfo: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFriendInfoBinding.inflate(inflater, container, false)
        friendInfo = args.user
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            friend = friendInfo
            btnFriendInfoOpenChat.setOnClickListener {
                getChatRoom()
            }
        }
        binding.viewModel = viewModel
        setFriendInfoUiState()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getChatRoom() {
        val myEmail = viewModel.getMyEmail()
        val member = listOf(myEmail, friendInfo.userEmail).sorted()
        viewModel.getChatRoom(member)
    }

    private fun setFriendInfoUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.friendInfoUiState.collect {
                    if(it.isNotExistChatRoom) {
                        val myEmail = viewModel.getMyEmail()
                        val member = listOf(
                            ChattingMember(userEmail = myEmail),
                            ChattingMember(userEmail = friendInfo.userEmail)
                        )
                        val newChatRoom = ChatRoom(member = member)
                        val action = FriendInfoDialogFragmentDirections.actionFriendInfoToChatRoom(newChatRoom)
                        findNavController().navigate(action)
                    }
                    if(it.isFoundChatRoom) {
                        navigateChatRoom()
                    }
                    if(it.isRemovedFriend) {
                        Snackbar.make(binding.root, getString(R.string.delete_friend_msg), Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    if(it.isNetworkError) {
                        Snackbar.make(binding.root,getString(R.string.network_error_msg), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateChatRoom() {
        val chatRoom = viewModel.foundChatRoom
        if(chatRoom != null) {
            val action = FriendInfoDialogFragmentDirections.actionFriendInfoToChatRoom(chatRoom)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}