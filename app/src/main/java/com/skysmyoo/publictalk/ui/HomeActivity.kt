package com.skysmyoo.publictalk.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.ActivityHomeBinding
import com.skysmyoo.publictalk.ui.home.friend.FriendListFragmentDirections
import com.skysmyoo.publictalk.utils.LanguageContextWrapper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        setNavigation()
    }

    override fun attachBaseContext(newBase: Context) {
        val locale = preferencesManager.getLocale()
        val context = LanguageContextWrapper.wrap(newBase, locale)
        super.attachBaseContext(context)
    }

    private fun setNavigation() {
        val navController =
            supportFragmentManager.findFragmentById(R.id.fcv_home)?.findNavController()
        navController?.let {
            binding.bnHome.setupWithNavController(it)
        }

        navController?.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNavigation = binding.bnHome
            val toolBar = binding.abHome
            when (destination.id) {
                R.id.navigation_friend_list -> {
                    setFriendListToolbar()
                    bottomNavigation.isVisible = true
                }

                R.id.navigation_chat_list -> {
                    setToolbar(getString(R.string.chat_list))
                    bottomNavigation.isVisible = true
                }

                R.id.navigation_setting -> {
                    setToolbar(getString(R.string.setting))
                    bottomNavigation.isVisible = true
                }

                R.id.navigation_chat_room -> {
                    toolBar.isVisible = false
                    bottomNavigation.isVisible = false
                }

                R.id.navigation_searching -> {
                    setToolbar(getString(R.string.searching_friend))
                    bottomNavigation.isVisible = false
                }
            }
        }
    }

    private fun setToolbar(title: String) {
        with(binding.abHome) {
            this.title = title
            menu.clear()
            isVisible = true
        }
    }

    private fun setFriendListToolbar() {
        setToolbar(getString(R.string.friend_list))
        binding.abHome.inflateMenu(R.menu.home_ab_menu)
        binding.abHome.setOnMenuItemClickListener {
            val navController =
                supportFragmentManager.findFragmentById(R.id.fcv_home)?.findNavController()
            val action = FriendListFragmentDirections.actionFriendListToSearching()
            navController?.navigate(action)
            true
        }
    }
}