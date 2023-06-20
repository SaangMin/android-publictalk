package com.skysmyoo.publictalk.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skysmyoo.publictalk.databinding.ActivityMainBinding
import com.skysmyoo.publictalk.utils.LanguageContextWrapper
import com.skysmyoo.publictalk.utils.LanguageSharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
    }

    override fun attachBaseContext(newBase: Context) {
        val locale = LanguageSharedPreferences.getLocale(newBase)
        val context = LanguageContextWrapper.wrap(newBase, locale)
        super.attachBaseContext(context)
    }
}