package com.skysmyoo.publictalk.data.model.local

import com.skysmyoo.publictalk.data.model.remote.User

object TestSampleData {

    val sampleUser = User(
        userEmail = "iu@gmail.com",
        userName = "아이유",
        userPhoneNumber = "01012345678",
        userProfileImage = "https://i.namu.wiki/i/R0AhIJhNi8fkU2Al72pglkrT8QenAaCJd1as-d_iY6MC8nub1iI5VzIqzJlLa-1uzZm--TkB-KHFiT-P-t7bEg.webp",
        userLanguage = "ko",
        userDeviceToken = "",
        userFriendIdList = emptyList(),
        userCreatedAt = "2023-06-26 15:16:38"
    )
}