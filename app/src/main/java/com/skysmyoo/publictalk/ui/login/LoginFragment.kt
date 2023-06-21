package com.skysmyoo.publictalk.ui.login

import android.os.Bundle
import android.view.View
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentLoginBinding

class LoginFragment : BaseFragment() {

    override val binding get() = _binding as FragmentLoginBinding
    override val layoutId: Int get() = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}