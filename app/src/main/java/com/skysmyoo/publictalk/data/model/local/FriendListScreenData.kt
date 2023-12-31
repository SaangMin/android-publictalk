package com.skysmyoo.publictalk.data.model.local

import com.skysmyoo.publictalk.data.model.remote.User

sealed class FriendListScreenData {

    data class Header(val header: String) : FriendListScreenData() {
        override val id: String
            get() = header
    }

    data class Friend(val friend: User) : FriendListScreenData() {
        override val id: String
            get() = friend.toString()
    }

    abstract val id: String
}