package com.skysmyoo.publictalk.ui.login

import android.os.Bundle
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentSettingInfoBinding

class SettingInfoFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingInfoBinding
    override val layoutId: Int get() = R.layout.fragment_setting_info

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}