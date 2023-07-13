package com.skysmyoo.publictalk.ui.loading

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.skysmyoo.publictalk.databinding.DialogLoadingBinding

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    
    init {
        DialogLoadingBinding.inflate(LayoutInflater.from(context), this, true)
    }
}