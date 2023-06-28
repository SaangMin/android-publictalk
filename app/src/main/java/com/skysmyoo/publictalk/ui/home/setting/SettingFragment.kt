package com.skysmyoo.publictalk.ui.home.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentSettingBinding

class SettingFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSettingBinding
    override val layoutId: Int get() = R.layout.fragment_setting

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSettingLogout.setOnClickListener {
            preferencesManager.clearUserData()

            val action = SettingFragmentDirections.actionSettingToStartActivity()
            findNavController().navigate(action)
            requireActivity().finish()
        }
    }
}