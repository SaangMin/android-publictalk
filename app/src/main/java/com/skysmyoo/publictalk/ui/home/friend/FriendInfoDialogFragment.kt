package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.databinding.DialogFriendInfoBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import com.skysmyoo.publictalk.utils.EventObserver
import dagger.hilt.android.AndroidEntryPoint

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
        notExistChatRoomObserver()
        foundChatRoomObserver()
        removeFriendEventObserver()
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

    private fun notExistChatRoomObserver() {
        viewModel.notExistChatRoom.observe(viewLifecycleOwner, EventObserver {
            val myEmail = viewModel.getMyEmail()
            val member = listOf(
                ChattingMember(userEmail = myEmail),
                ChattingMember(userEmail = friendInfo.userEmail)
            )
            val newChatRoom = ChatRoom(member = member)
            val action = FriendInfoDialogFragmentDirections.actionFriendInfoToChatRoom(newChatRoom)
            findNavController().navigate(action)
        })
    }

    private fun foundChatRoomObserver() {
        viewModel.foundChatRoom.observe(viewLifecycleOwner, EventObserver {
            val action = FriendInfoDialogFragmentDirections.actionFriendInfoToChatRoom(it)
            findNavController().navigate(action)
        })
    }

    private fun removeFriendEventObserver() {
        viewModel.removeFriendEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(R.string.delete_friend_msg), Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}