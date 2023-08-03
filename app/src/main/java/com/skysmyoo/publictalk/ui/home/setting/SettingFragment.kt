package com.skysmyoo.publictalk.ui.home.setting

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.Language
import com.skysmyoo.publictalk.databinding.FragmentSettingBinding
import com.skysmyoo.publictalk.ui.HomeActivity
import com.skysmyoo.publictalk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentSettingBinding
    override val layoutId: Int get() = R.layout.fragment_setting

    private val viewModel: HomeViewModel by viewModels()
    private var userLanguage: Language? = null
    private lateinit var pickImage: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPickImage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        viewModel.getMyInfo()

        setUiState()
        setNotificationSwitch()
        binding.btnSettingDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun setUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.settingUiState.collect {
                        if (it.isGettingMyInfo) {
                            binding.user = viewModel.myInfo
                            userLanguage = it.language
                            setSpinner()
                        }
                        if (it.isLogoutClick) {
                            restartApp()
                        }
                        if (it.isImageClicked) {
                            pickImage.launch("image/*")
                        }
                        if (it.isEdit) {
                            restartHome()
                        }
                        if (it.isFailed) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.edit_error_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        if (it.isDeleteAccount) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.delete_account_accept_msg),
                                Snackbar.LENGTH_SHORT
                            ).show()
                            restartApp()
                        }
                    }
                }
            }
        }
    }

    private fun setSpinner() {
        val languageList = resources.getStringArray(R.array.user_language)
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            languageList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val myLanguagePosition = userLanguage?.toPosition() ?: 0

        with(binding.spSettingLanguage) {
            this.adapter = adapter
            setSelection(myLanguagePosition)
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    userLanguage = Language.fromPosition(position)
                    binding.language = userLanguage?.code ?: preferencesManager.getLocale()
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

    private fun restartHome() {
        val action = SettingFragmentDirections.actionSettingToHome()
        findNavController().navigate(action)
        requireActivity().finish()
    }

    private fun restartApp() {
        val action = SettingFragmentDirections.actionSettingToStartActivity()
        findNavController().navigate(action)
        requireActivity().finish()
    }

    private fun setNotificationSwitch() {
        val isAlarm = preferencesManager.getNotification()
        binding.swSettingNotification.isChecked = isAlarm

        binding.swSettingNotification.setOnCheckedChangeListener { _, isChecked ->
            val activity = requireActivity() as HomeActivity
            if (isChecked) {
                activity.registerNotification()
                preferencesManager.setNotification(true)
                Snackbar.make(
                    binding.root,
                    getString(R.string.notification_on_msg),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                activity.unregisterNotification()
                preferencesManager.setNotification(false)
                Snackbar.make(
                    binding.root,
                    getString(R.string.notification_off_msg),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setPickImage() {
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                requireActivity().contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    cursor.moveToFirst()
                    binding.ivSettingMyProfile.setImageURI(it)
                    binding.imageUri = it
                }
            } ?: run {
                Log.d(TAG, "No media selected")
            }
        }
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.delete_account_msg))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    companion object {
        private const val TAG = "SettingFragment"
    }
}