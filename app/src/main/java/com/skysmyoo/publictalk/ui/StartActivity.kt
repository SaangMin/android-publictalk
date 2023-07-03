package com.skysmyoo.publictalk.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skysmyoo.publictalk.databinding.ActivityStartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
    }
}