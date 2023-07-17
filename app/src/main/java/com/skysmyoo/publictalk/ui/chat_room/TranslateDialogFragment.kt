package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.skysmyoo.publictalk.databinding.DialogTranslateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TranslateDialogFragment : DialogFragment() {

    private var _binding: DialogTranslateBinding? = null
    private val binding get() = _binding!!

    private val args: TranslateDialogFragmentArgs by navArgs()
    private lateinit var body: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTranslateBinding.inflate(inflater, container, false)
        body = args.body
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTranslateResult.text = body
        binding.btnTranslateSend.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnTranslateCancel.setOnClickListener {
            findNavController().navigateUp()
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