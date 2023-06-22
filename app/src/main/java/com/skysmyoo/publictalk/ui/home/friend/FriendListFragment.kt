package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData.Friend
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData.Header
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.databinding.FragmentFriendListBinding
import com.skysmyoo.publictalk.di.ServiceLocator
import kotlinx.coroutines.launch

class FriendListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentFriendListBinding
    override val layoutId: Int get() = R.layout.fragment_friend_list
    private lateinit var friendListAdapter: FriendListAdapter
    private val repository = UserRepository(
        UserLocalDataSource(ServiceLocator.userDao)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendListAdapter = FriendListAdapter()
        binding.rvFriendList.adapter = friendListAdapter
        setDefaultAdapterItem(friendListAdapter)

    }

    private fun setDefaultAdapterItem(adapter: FriendListAdapter) {
        lifecycleScope.launch {
            val myInfo = repository.getMyInfo(FirebaseData.user?.email!!)
            if (myInfo != null) {
                val defaultList = listOf(
                    Header(getString(R.string.mine)),
                    Friend(myInfo),
                    Header(getString(R.string.friend_label))
                )
                adapter.submitList(defaultList)
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.load_my_info_error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "FriendListFragment"
    }
}