package com.skysmyoo.publictalk.data.source.local

import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(
    private val chatModelDao: ChatModelDao,
    private val preferencesManager: SharedPreferencesManager
) {
}