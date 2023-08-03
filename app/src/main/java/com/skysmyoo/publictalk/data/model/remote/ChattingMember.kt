package com.skysmyoo.publictalk.data.model.remote

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ChattingMember(
    val isChatting: Boolean = false,
    val userEmail: String = "",
) : Parcelable
