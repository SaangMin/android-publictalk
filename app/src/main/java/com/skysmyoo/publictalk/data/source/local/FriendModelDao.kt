package com.skysmyoo.publictalk.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.skysmyoo.publictalk.data.model.local.SavedFriend
import com.skysmyoo.publictalk.data.model.remote.User

@Dao
interface FriendModelDao {

    @Insert
    suspend fun insertFriend(friend: SavedFriend)

    @Query("DELETE FROM saved_friend_models")
    suspend fun clearFriends()

    @Query("DELETE FROM saved_friend_models WHERE friendData = :friend")
    suspend fun removeFriend(friend: User)

    @Query("SELECT * FROM saved_friend_models")
    suspend fun getFriendList(): List<SavedFriend>

}