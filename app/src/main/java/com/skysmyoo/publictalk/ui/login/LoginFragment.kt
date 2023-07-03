package com.skysmyoo.publictalk.ui.login

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    override val binding get() = _binding as FragmentLoginBinding
    override val layoutId: Int get() = R.layout.fragment_login

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signInClient: SignInClient
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var legacyLauncher: ActivityResultLauncher<Intent>
    private var idToken: String? = null
    private val viewModel: LoginViewModel by viewModels()
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
        .requestEmail()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setGoogleLoginService()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            beginLogin(oneTapLauncher, legacyLauncher)
        }
        existUserEmailObserver()
        googleLoginObserver()
    }

    private fun setGoogleLoginService() {
        signInClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
        oneTapLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    try {
                        val signInCredential = signInClient.getSignInCredentialFromIntent(data)
                        idToken = signInCredential.googleIdToken
                        viewModel.validateExistUser(signInCredential.id)
                    } catch (e: ApiException) {
                        Log.e(TAG, "Couldn't get credential from result.(${e.localizedMessage}")
                    }
                } else {
                    Log.e(TAG, "one tap login failed: $result")
                    Snackbar.make(
                        binding.root,
                        getString(R.string.login_error_msg_retry),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        legacyLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        idToken = account.idToken
                        viewModel.validateExistUser(account.email)
                    } catch (e: ApiException) {
                        Log.w(TAG, "Google sign in failed", e)
                    }
                } else {
                    Log.e(TAG, "legacy login failed: ${result.resultCode}")
                    Snackbar.make(
                        binding.root,
                        getString(R.string.login_error_msg_retry),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        firebaseAuth = Firebase.auth
    }

    private fun beginLogin(
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        legacyLauncher: ActivityResultLauncher<Intent>
    ) {
        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    oneTapLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                Log.e(TAG, "one tap login failed: $e")
                val signInIntent = googleSignInClient.signInIntent
                googleSignInClient.signOut().addOnCompleteListener {
                    legacyLauncher.launch(signInIntent)
                }
            }
    }

    private fun googleLoginObserver() {
        viewModel.googleLoginEvent.observe(viewLifecycleOwner) {
            if (idToken != null) {
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(authCredential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.login_success_msg),
                                Toast.LENGTH_SHORT
                            ).show()
                            FirebaseData.setUserInfo()
                            setNavigation()
                        } else {
                            Log.w(TAG, "signInWithCredential failed : ${task.exception}")
                        }
                    }
            }
        }
    }

    private fun existUserEmailObserver() {
        viewModel.isExistUser.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.exist_user_info_msg),
                    Toast.LENGTH_SHORT
                ).show()
                val action = LoginFragmentDirections.actionLoginToHome()
                findNavController().navigate(action)
                requireActivity().finish()
            }
        }
    }

    private fun setNavigation() {
        val action = LoginFragmentDirections.actionLoginToSettingInfo()
        findNavController().navigate(action)
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}