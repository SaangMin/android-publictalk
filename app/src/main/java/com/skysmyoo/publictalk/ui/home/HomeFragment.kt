package com.skysmyoo.publictalk.ui.home

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.skysmyoo.publictalk.BaseFragment
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    override val binding get() = _binding!! as FragmentHomeBinding
    override val layoutId: Int get() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController =
            childFragmentManager.findFragmentById(R.id.fcv_home)?.findNavController()
        navController?.let {
            binding.bnHome.setupWithNavController(it)
            setAppbar(navController)
        }
    }

    private fun setAppbar(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_friend_list -> {
                    setToolbar(getString(R.string.friend_list))
                    binding.abHome.inflateMenu(R.menu.home_ab_menu)
                }

                R.id.navigation_chat_list -> {
                    setToolbar(getString(R.string.chat_list))
                }

                R.id.navigation_setting -> {
                    setToolbar(getString(R.string.setting))
                }
            }
        }
    }

    private fun setToolbar(title: String) {
        with (binding.abHome) {
            this.title = title
            menu.clear()
        }
    }
}