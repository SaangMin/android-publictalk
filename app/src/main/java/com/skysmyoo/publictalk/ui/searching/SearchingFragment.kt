package com.skysmyoo.publictalk.ui.searching

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentSearchingBinding
import com.skysmyoo.publictalk.utils.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchingFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSearchingBinding
    override val layoutId: Int get() = R.layout.fragment_searching
    private val viewModel: SearchingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        notExistUserObserver()
        foundUserObserver()
    }

    private fun notExistUserObserver() {
        viewModel.notExistUserEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_exist_user_error_msg),
                Toast.LENGTH_SHORT
            ).show()
            binding.clSearchingFriend.isVisible = false
        })
    }

    private fun foundUserObserver() {
        viewModel.foundUser.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(),getString(R.string.find_success_user_msg),Toast.LENGTH_SHORT).show()
            binding.clSearchingFriend.isVisible = true
            binding.foundUser = it
        })
    }
}