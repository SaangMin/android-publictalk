package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
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

        friendListAdapter = FriendListAdapter()
        binding.rvFriendList.adapter = friendListAdapter
        viewModel.setAdapterItemList(getString(R.string.mine), getString(R.string.friend_label))
        adapterItemListObserver()
    }

    private fun adapterItemListObserver() {
        viewModel.adapterItemList.observe(viewLifecycleOwner, EventObserver {
            friendListAdapter.submitList(it)
        })
    }

    companion object {
        private const val TAG = "FriendListFragment"
    }
}