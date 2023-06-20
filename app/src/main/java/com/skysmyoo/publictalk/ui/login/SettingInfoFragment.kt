package com.skysmyoo.publictalk.ui.login

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.local.Language
import com.skysmyoo.publictalk.databinding.FragmentSettingInfoBinding

class SettingInfoFragment : BaseFragment() {

    override val binding get() = _binding as FragmentSettingInfoBinding
    override val layoutId: Int get() = R.layout.fragment_setting_info
    private var userLanguage: Language? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpinner()
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
}