package com.skysmyoo.publictalk.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.UserRemoteDataSource
import com.skysmyoo.publictalk.databinding.FragmentSplashBinding
import com.skysmyoo.publictalk.di.ServiceLocator
import com.skysmyoo.publictalk.ui.login.LoginViewModel

class SplashFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSplashBinding
    override val layoutId: Int get() = R.layout.fragment_splash

    private val viewModel by viewModels<LoginViewModel> {
        LoginViewModel.provideFactory(
            UserRepository(
                UserLocalDataSource(ServiceLocator.userDao),
                UserRemoteDataSource(ServiceLocator.apiClient)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseData.setDeviceToken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        validateAlreadyLogin()
        existUserEmailObserver()
    }

    private fun validateAlreadyLogin() {
        val email = preferencesManager.getMyEmail()
        Log.d(TAG, "$email")
        if (email.isNullOrEmpty()) {
            val action = SplashFragmentDirections.actionSplashToLogin()
            findNavController().navigate(action)
        } else {
            viewModel.validateExistUser(email)
        }
    }

    private fun existUserEmailObserver() {
        viewModel.isExistUser.observe(viewLifecycleOwner) {
            Log.d(TAG, "$it")
            if (it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.exist_user_info_msg),
                    Toast.LENGTH_SHORT
                ).show()

                val action = SplashFragmentDirections.actionSplashToHome()
                findNavController().navigate(action)
                requireActivity().finish()
            }
        }
    }

    companion object {
        private const val TAG = "SplashFragment"
    }

}