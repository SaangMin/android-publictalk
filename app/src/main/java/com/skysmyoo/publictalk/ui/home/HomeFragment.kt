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
        val appbar = binding.abHome
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_friend_list -> {
                    appbar.title = getString(R.string.friend_list)
                    appbar.menu.clear()
                    appbar.inflateMenu(R.menu.home_ab_menu)
                }

                R.id.navigation_chat_list -> {
                    appbar.title = getString(R.string.chat_list)
                    appbar.menu.clear()
                }

                R.id.navigation_setting -> {
                    appbar.title = getString(R.string.setting)
                    appbar.menu.clear()
                }
            }
        }
    }
}