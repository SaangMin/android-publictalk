package com.skysmyoo.publictalk.ui.home

import android.os.Bundle
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentHomeBinding
    override val layoutId: Int get() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}