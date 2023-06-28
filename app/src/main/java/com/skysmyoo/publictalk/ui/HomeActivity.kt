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
import com.skysmyoo.publictalk.utils.LanguageContextWrapper

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
            when (destination.id) {
                R.id.navigation_friend_list -> {
                    setToolbar(getString(R.string.friend_list))
                    binding.abHome.inflateMenu(R.menu.home_ab_menu)
                    binding.bnHome.isVisible = true
                }

                R.id.navigation_chat_list -> {
                    setToolbar(getString(R.string.chat_list))
                    binding.bnHome.isVisible = true
                }

                R.id.navigation_setting -> {
                    setToolbar(getString(R.string.setting))
                    binding.bnHome.isVisible = true
                }
            }
        }
    }

    private fun setToolbar(title: String) {
        with(binding.abHome) {
            this.title = title
            menu.clear()
        }
    }
}