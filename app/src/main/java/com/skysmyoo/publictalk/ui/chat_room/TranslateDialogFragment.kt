package com.skysmyoo.publictalk.ui.chat_room

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
import com.skysmyoo.publictalk.databinding.DialogTranslateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TranslateDialogFragment : DialogFragment() {

    private var _binding: DialogTranslateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatRoomViewModel by viewModels()
    private val args: TranslateDialogFragmentArgs by navArgs()
    private lateinit var body: String
    private lateinit var chatRoom: ChatRoom

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTranslateBinding.inflate(inflater, container, false)
        body = args.body
        chatRoom = args.chatRoom
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTranslateResult.text = body
        setLayout()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setLayout() {
        viewModel.getRoomKey(chatRoom)

        binding.btnTranslateSend.setOnClickListener {
            viewModel.sendMessage(chatRoom, body)
        }
        binding.btnTranslateCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        setTranslateState()
    }

    private fun setTranslateState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isSent.collect {
                        if (it) {
                            findNavController().navigateUp()
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
                launch {
                    viewModel.isSendFailed.collect{
                        if(it) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.message_send_failed_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}