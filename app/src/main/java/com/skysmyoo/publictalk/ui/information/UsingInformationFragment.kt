package com.skysmyoo.publictalk.ui.information

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentUsingInformationBinding

class UsingInformationFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentUsingInformationBinding
    override val layoutId: Int get() = R.layout.fragment_using_information

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUsingClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}