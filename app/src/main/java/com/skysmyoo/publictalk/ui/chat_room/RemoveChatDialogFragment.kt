package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.skysmyoo.publictalk.databinding.DialogRemoveChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemoveChatDialogFragment : DialogFragment() {

    private var _binding: DialogRemoveChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRemoveChatBinding.inflate(inflater, container, false)
        return binding.root
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