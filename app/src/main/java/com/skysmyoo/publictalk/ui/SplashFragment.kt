package com.skysmyoo.publictalk.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.databinding.FragmentSplashBinding
import com.skysmyoo.publictalk.ui.login.LoginViewModel
import com.skysmyoo.publictalk.utils.isNetworkAvailable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSplashBinding
    override val layoutId: Int get() = R.layout.fragment_splash

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseData.setDeviceToken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        validateAlreadyLogin()
        setSplashUiState()
    }

    private fun validateAlreadyLogin() {
        val email = viewModel.getMyEmail()
        if (email.isNullOrEmpty()) {
            navigateToLogin()
        } else {
            if (isNetworkAvailable(requireContext())) {
                viewModel.validateExistUser(email)
            } else {
                viewModel.localLogin()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.offline_mode_login), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setSplashUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.splashUiState.collect {
                    if (it.isExist == true) {
                        val action = SplashFragmentDirections.actionSplashToHome()
                        findNavController().navigate(action)
                        requireActivity().finish()
                    } else if (it.isExist == false) {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val action = SplashFragmentDirections.actionSplashToLogin()
        findNavController().navigate(action)
    }
}