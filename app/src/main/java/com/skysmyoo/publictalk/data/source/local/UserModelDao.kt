package com.skysmyoo.publictalk.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.skysmyoo.publictalk.data.model.remote.User

@Dao
interface UserModelDao {

    @Insert
    suspend fun insertUser(user: User)

    @Query("DELETE FROM saved_user_models")
    suspend fun clearUser()

    @Query("SELECT * FROM saved_user_models WHERE userEmail = :email LIMIT 1")
    suspend fun getMyInfo(email: String): User?

    @Query("SELECT * FROM saved_user_models")
    suspend fun getUserList(): List<User>?

    @Update
    suspend fun updateUser(user: User)
}