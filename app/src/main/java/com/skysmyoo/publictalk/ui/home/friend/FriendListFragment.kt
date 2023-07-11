package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentFriendListBinding
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import com.skysmyoo.publictalk.utils.EventObserver
import dagger.hilt.android.AndroidEntryPoint

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
        adapterItemListObserver()
        friendClickEventObserver()
        myInfoClickEventObserver()
    }

    private fun adapterItemListObserver() {
        viewModel.adapterItemList.observe(viewLifecycleOwner, EventObserver {
            friendListAdapter.submitList(it)
        })
    }

    private fun friendClickEventObserver() {
        viewModel.friendClickEvent.observe(viewLifecycleOwner, EventObserver {
            val friend = viewModel.clickedFriend
            if(friend != null) {
                val action = FriendListFragmentDirections.actionFriendListToFriendInfo(friend)
                findNavController().navigate(action)
            }
        })
    }

    private fun myInfoClickEventObserver() {
        viewModel.myInfoClickEvent.observe(viewLifecycleOwner, EventObserver {
            val action = FriendListFragmentDirections.actionFriendListToSetting()
            findNavController().navigate(action)
        })
    }

    companion object {
        private const val TAG = "FriendListFragment"
    }
}