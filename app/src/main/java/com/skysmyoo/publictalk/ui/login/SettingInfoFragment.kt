package com.skysmyoo.publictalk.ui.login

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.Language
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.user
import com.skysmyoo.publictalk.databinding.FragmentSettingInfoBinding
import com.skysmyoo.publictalk.ui.loading.LoadingDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingInfoFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingInfoBinding
    override val layoutId: Int get() = R.layout.fragment_setting_info

    private lateinit var pickImage: ActivityResultLauncher<String>
    private var imageUri: Uri? = null
    private var userLanguage: Language? = null
    private val loadingDialog by lazy { LoadingDialogFragment() }
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPickImage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.email = user?.email
        binding.viewModel = viewModel
        setSpinner()
        onProfileImageClickObserver()
        isLoadingObserver()
        submitRequiredObserver()
        submitUserObserver()
        failedMessageObserver()
    }

    private fun onProfileImageClickObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addImageEvent.collect {
                    pickImage.launch("image/*")
                }
            }
        }
    }

    private fun isLoadingObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect {
                    if (it) {
                        loadingDialog.show(parentFragmentManager, TAG)
                    } else {
                        if (loadingDialog.isAdded) {
                            loadingDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun failedMessageObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.failedMessage.collect {
                    Snackbar.make(binding.root, getString(R.string.join_fail_error_msg), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun submitRequiredObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notRequiredEvent.collect {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.not_required_error_msg),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun submitUserObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitEvent.collect {
                    viewModel.submitUser(imageUri, userLanguage?.code ?: "ko") {
                        startHomeActivity()
                    }
                }
            }
        }
    }

    private fun startHomeActivity() {
        val action = SettingInfoFragmentDirections.actionSettingInfoToHome()
        findNavController().navigate(action)
        requireActivity().finish()
    }

    private fun setPickImage() {
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                requireActivity().contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    cursor.moveToFirst()
                    imageUri = it
                    binding.ivSettingInfoProfile.setImageURI(it)
                }
            } ?: run {
                Log.d(TAG, "No media selected")
            }
        }
    }

    private fun setSpinner() {
        val languageList = resources.getStringArray(R.array.user_language)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            languageList
        )
        with(binding.spSettingInfoLanguage) {
            this.adapter = adapter
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    userLanguage = Language.fromPosition(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Snackbar.make(
                        binding.root,
                        context.getString(R.string.nothing_select_language_msg),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val TAG = "SettingInfoFragment"
    }
}