package com.skysmyoo.publictalk.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import com.skysmyoo.publictalk.R

@BindingAdapter("profileImage")
fun loadUserImage(view: ImageView, image: String?) {
    view.load(image) {
        error(R.drawable.icon_profile_image)
        fallback(R.drawable.icon_profile_image)
    }
}