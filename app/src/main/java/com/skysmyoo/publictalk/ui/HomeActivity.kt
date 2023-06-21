package com.skysmyoo.publictalk.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skysmyoo.publictalk.databinding.ActivityHomeBinding
import com.skysmyoo.publictalk.utils.LanguageContextWrapper
import com.skysmyoo.publictalk.utils.LanguageSharedPreferences

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
    }

    override fun attachBaseContext(newBase: Context) {
        val locale = LanguageSharedPreferences.getLocale(newBase)
        val context = LanguageContextWrapper.wrap(newBase, locale)
        super.attachBaseContext(context)
    }
}