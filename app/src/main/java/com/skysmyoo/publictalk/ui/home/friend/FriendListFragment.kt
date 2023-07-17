package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.databinding.FragmentFriendListBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentFriendListBinding
    override val layoutId: Int get() = R.layout.fragment_friend_list

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var friendListAdapter: FriendListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendListAdapter = FriendListAdapter(viewModel)
        binding.rvFriendList.adapter = friendListAdapter
        viewModel.setAdapterItemList(getString(R.string.mine), getString(R.string.friend_label))
        setFriendListUiState()
    }

    private fun setFriendListUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.friendListUiState.collect {
                    if (it.isFriendClicked) {
                        val friend = viewModel.clickedFriend
                        if (friend != null) {
                            showFriendInfo(friend)
                        }
                    }
                    if (it.isMyInfoClicked) {
                        showSettingFragment()
                    }
                    if (it.adapterItemList.isNotEmpty()) {
                        friendListAdapter.submitList(it.adapterItemList)
                    }
                }
            }
        }
    }

    private fun showFriendInfo(friend: User) {
        if (findNavController().currentDestination?.id == R.id.navigation_friend_list) {
            val action = FriendListFragmentDirections.actionFriendListToFriendInfo(friend)
            findNavController().navigate(action)
        }
    }

    private fun showSettingFragment() {
        if (findNavController().currentDestination?.id == R.id.navigation_friend_list) {
            val action = FriendListFragmentDirections.actionFriendListToSetting()
            findNavController().navigate(action)
        }
    }

    companion object {
        private const val TAG = "FriendListFragment"
    }
}