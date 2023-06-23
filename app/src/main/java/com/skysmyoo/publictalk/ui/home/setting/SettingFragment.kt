package com.skysmyoo.publictalk.ui.home.setting

import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentSettingBinding

class SettingFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSettingBinding
    override val layoutId: Int get() = R.layout.fragment_setting
}