package com.skysmyoo.publictalk.data.model.local

import com.skysmyoo.publictalk.data.model.remote.request.User

sealed class FriendListScreenData {

    data class Header(val header: String) : FriendListScreenData() {
        override val id: String
            get() = header
    }

    data class FriendListItem(val friend: User) : FriendListScreenData() {
        override val id: String
            get() = friend.toString()
    }

    abstract val id: String
}