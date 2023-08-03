package com.skysmyoo.publictalk.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.databinding.ActivityHomeBinding
import com.skysmyoo.publictalk.receiver.MyBroadcastReceiver
import com.skysmyoo.publictalk.ui.home.friend.FriendListFragmentDirections
import com.skysmyoo.publictalk.utils.Constants
import com.skysmyoo.publictalk.utils.LanguageContextWrapper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        receiver = MyBroadcastReceiver()
        registerNotification()
        binding.lifecycleOwner = this
        setNavigation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setNotificationPermission()
        }
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

                R.id.navigation_using -> {
                    setToolbar(getString(R.string.how_to_use_label))
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setNotificationPermission() {
        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!isGranted) {
                    val shouldShowRational = ActivityCompat.shouldShowRequestPermissionRationale(
                        this@HomeActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (!shouldShowRational) {
                        preferencesManager.setNotification(false)
                    }
                }
            }
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun registerNotification() {
        registerReceiver(receiver, IntentFilter(Constants.MY_NOTIFICATION))
    }

    fun unregisterNotification() {
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        unregisterNotification()
        super.onDestroy()
    }
}