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
import androidx.lifecycle.lifecycleScope
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.PublicTalkApplication
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setDeviceToken
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setUserInfo
import com.skysmyoo.publictalk.data.source.remote.UserRemoteDataSource
import com.skysmyoo.publictalk.databinding.FragmentLoginBinding
import com.skysmyoo.publictalk.di.ServiceLocator
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment() {

    override val binding get() = _binding as FragmentLoginBinding
    override val layoutId: Int get() = R.layout.fragment_login

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signInClient: SignInClient
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var legacyLauncher: ActivityResultLauncher<Intent>
    private val preferencesManager = PublicTalkApplication.preferencesManager
    private val repository = UserRepository(
        UserLocalDataSource(ServiceLocator.userDao),
        UserRemoteDataSource(ServiceLocator.apiClient)
    )

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
        .requestEmail()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setGoogleLoginService()
        setDeviceToken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        validateAlreadyLogin()

        binding.btnLogin.setOnClickListener {
            beginLogin(oneTapLauncher, legacyLauncher)
        }
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
                        val idToken = signInCredential.googleIdToken
                        submitToken(idToken)
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
                        submitToken(account?.idToken)
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

    private fun submitToken(idToken: String?) {
        setUserInfo()
        if (!isExistUser(FirebaseData.user?.email)) {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.login_success_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                        setNavigation()
                    } else {
                        Log.w(TAG, "signInWithCredential failed : ${task.exception}")
                    }
                }
        }
    }

    private fun validateAlreadyLogin() {
        val email = preferencesManager.getMyEmail()
        if (email == null) {
            return
        } else {
            if (isExistUser(email)) {
                lifecycleScope.launch {
                    val myInfo = repository.getMyInfo(email)
                    if (myInfo == null) {
                        return@launch
                    } else {
                        preferencesManager.setLocale(myInfo.userLanguage)

                        val action = LoginFragmentDirections.actionLoginToHome()
                        findNavController().navigate(action)
                        requireActivity().finish()
                    }
                }
            } else {
                return
            }
        }
    }

    private fun isExistUser(email: String?): Boolean {
        var result = false
        val ref = Firebase.database(BuildConfig.BASE_URL).getReference("users")
        ref.orderByChild("userEmail").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.exist_user_info_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                        lifecycleScope.launch {
                            val user = snapshot.children.firstOrNull()?.getValue(User::class.java)
                                ?: return@launch
                            user.userDeviceToken = FirebaseData.token ?: return@launch
                            with(repository) {
                                clearUser()
                                insertUser(user)
                            }
                            setUserInfo()
                            preferencesManager.saveMyEmail(user.userEmail)
                            preferencesManager.setLocale(user.userLanguage)
                            result = true
                            val action = LoginFragmentDirections.actionLoginToHome()
                            findNavController().navigate(action)
                            requireActivity().finish()
                        }
                    } else {
                        result = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "LoadUser:onCancelled: $error")
                    result = false
                }
            })
        return result
    }

    private fun setNavigation() {
        val action = LoginFragmentDirections.actionLoginToSettingInfo()
        findNavController().navigate(action)
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}