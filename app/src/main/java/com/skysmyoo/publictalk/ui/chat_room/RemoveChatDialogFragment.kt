package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.skysmyoo.publictalk.databinding.DialogRemoveChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RemoveChatDialogFragment : DialogFragment() {

    private var _binding: DialogRemoveChatBinding? = null
    private val binding get() = _binding!!
    private val args: RemoveChatDialogFragmentArgs by navArgs()
    private val viewModel: ChatRoomViewModel by viewModels()
    private lateinit var chatRoom: ChatRoom

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRemoveChatBinding.inflate(inflater, container, false)
        chatRoom = args.chatRoom
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRemoveChatObserver()
        binding.btnRemoveChatCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setRemoveChatObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isSuccessDeleteChat.collect {
                        if(it) {
                            Toast.makeText(requireContext(), getString(R.string.success_remove_chat_msg), Toast.LENGTH_SHORT).show()
                            val action = RemoveChatDialogFragmentDirections.actionRemoveChatToChatList()
                            findNavController().navigate(action)
                        }
                    }
                }
                launch {
                    viewModel.isFailedDeleteChat.collect{
                        if(it) {
                            Snackbar.make(binding.root, getString(R.string.failure_remove_chat_msg),Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}