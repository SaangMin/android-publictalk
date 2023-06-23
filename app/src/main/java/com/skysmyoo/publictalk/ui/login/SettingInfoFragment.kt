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
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.Language
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.user
import com.skysmyoo.publictalk.data.source.remote.LoginRemoteDataSource
import com.skysmyoo.publictalk.databinding.FragmentSettingInfoBinding
import com.skysmyoo.publictalk.di.ServiceLocator
import com.skysmyoo.publictalk.ui.loading.LoadingDialogFragment

class SettingInfoFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingInfoBinding
    override val layoutId: Int get() = R.layout.fragment_setting_info

    private lateinit var pickImage: ActivityResultLauncher<String>
    private var imageUri: Uri? = null
    private var userLanguage: Language? = null
    private val loadingDialog by lazy { LoadingDialogFragment() }
    private val viewModel by viewModels<UserViewModel> {
        UserViewModel.provideFactory(
            UserRepository(
                LoginRemoteDataSource(ServiceLocator.apiClient)
            )
        )
    }

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
    }

    private fun onProfileImageClickObserver() {
        viewModel.addImageEvent.observe(viewLifecycleOwner) {
            pickImage.launch("image/*")
        }
    }

    private fun isLoadingObserver() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                loadingDialog.show(parentFragmentManager, TAG)
            } else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun submitRequiredObserver() {
        viewModel.notRequiredEvent.observe(viewLifecycleOwner) {
            Snackbar.make(
                binding.root,
                getString(R.string.not_required_error_msg),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun submitUserObserver() {
        viewModel.submitEvent.observe(viewLifecycleOwner) {
            viewModel.submitUser(imageUri, userLanguage?.code ?: "ko")
        }
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