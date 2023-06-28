package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData.Friend
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData.Header
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.databinding.FragmentFriendListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FriendListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentFriendListBinding
    override val layoutId: Int get() = R.layout.fragment_friend_list

    @Inject
    lateinit var repository: UserRepository

    private lateinit var friendListAdapter: FriendListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendListAdapter = FriendListAdapter()
        binding.rvFriendList.adapter = friendListAdapter
        setDefaultAdapterItem(friendListAdapter)

    }

    private fun setDefaultAdapterItem(adapter: FriendListAdapter) {
        val email = repository.getMyEmail()
        if (email != null) {
            lifecycleScope.launch {
                val myInfo = repository.getMyInfo(email)
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
    }

    companion object {
        private const val TAG = "FriendListFragment"
    }
}