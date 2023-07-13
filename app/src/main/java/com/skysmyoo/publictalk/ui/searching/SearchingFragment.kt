package com.skysmyoo.publictalk.ui.searching

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentSearchingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchingFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSearchingBinding
    override val layoutId: Int get() = R.layout.fragment_searching
    private val viewModel: SearchingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        setSearchingUiState()
    }

    private fun setSearchingUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchingUiState.collect {
                    if (it.isNotExistUser) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.not_exist_user_error_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.clSearchingFriend.isVisible = false
                    }
                    if (it.isFoundUser) {
                        val user = viewModel.gettingUser
                        Snackbar.make(
                            binding.root,
                            getString(R.string.find_success_user_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.clSearchingFriend.isVisible = true
                        binding.foundUser = user
                    }
                    if (it.isAlreadyFriend) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.already_friend_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    if (it.isAddedFriend) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.add_friend_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    if (it.isNetworkError) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.network_error_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    if(it.isFailedAddFriend) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.add_friend_error_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}