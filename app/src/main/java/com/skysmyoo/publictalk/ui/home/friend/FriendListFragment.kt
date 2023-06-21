package com.skysmyoo.publictalk.ui.home.friend

import android.os.Bundle
import android.util.Log
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentFriendListBinding

class FriendListFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentFriendListBinding
    override val layoutId: Int get() = R.layout.fragment_friend_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}