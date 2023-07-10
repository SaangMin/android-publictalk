package com.skysmyoo.publictalk.ui.searching

import android.os.Bundle
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentSearchingBinding

class SearchingFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSearchingBinding
    override val layoutId: Int get() = R.layout.fragment_searching

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}