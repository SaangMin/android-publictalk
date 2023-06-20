package com.skysmyoo.publictalk.di

import com.skysmyoo.publictalk.data.source.remote.ApiClient
import com.skysmyoo.publictalk.data.source.remote.FcmClient
import com.squareup.moshi.Moshi

object ServiceLocator {
    val moshi: Moshi by lazy { Moshi.Builder().build() }
    val apiClient: ApiClient by lazy { ApiClient.create() }
    val fcmClient: FcmClient by lazy { FcmClient.create() }
}