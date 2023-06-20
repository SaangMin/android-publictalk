package com.skysmyoo.publictalk.di

import com.skysmyoo.publictalk.data.source.remote.ApiClient
import com.squareup.moshi.Moshi

object ServiceLocator {
    val moshi: Moshi by lazy { Moshi.Builder().build() }
    val apiClient: ApiClient by lazy { ApiClient.create() }
}