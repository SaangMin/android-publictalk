package com.skysmyoo.publictalk.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.skysmyoo.publictalk.data.model.remote.ChatRoom

@Dao
interface ChatModelDao {

    @Insert
    suspend fun insertChatRoom(chatRoom: ChatRoom)

    @Query("DELETE FROM saved_chat_rooms")
    suspend fun clearChatRooms()

    @Query("SELECT * FROM saved_chat_rooms")
    suspend fun getChatRoomList(): List<ChatRoom>?
}