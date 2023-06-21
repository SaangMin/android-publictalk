package com.skysmyoo.publictalk.ui.loading

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.skysmyoo.publictalk.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }
}