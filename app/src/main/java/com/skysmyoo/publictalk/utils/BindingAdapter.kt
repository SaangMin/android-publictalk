package com.skysmyoo.publictalk.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

@BindingAdapter("isVisible")
fun isVisible(view: TextView, unreadMessage: Int) {
    if (unreadMessage > 0) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.INVISIBLE
    }
}

@BindingAdapter("isReading")
fun isReading(view: TextView, isReading: Boolean) {
    if (isReading) {
        view.visibility = View.INVISIBLE
    } else {
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("isLoading")
fun isLoading(view: View, isLoading: Boolean) {
    if (isLoading) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}