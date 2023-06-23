package com.skysmyoo.publictalk.data.source

import android.net.Uri
import com.skysmyoo.publictalk.data.model.remote.request.User
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.storage
import com.skysmyoo.publictalk.data.source.remote.LoginRemoteDataSource
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class UserRepository(
    private val remoteDataSource: LoginRemoteDataSource
) {

    private val currentTime = TimeUtil.getCurrentDateString()

    suspend fun putUser(auth: String, user: User): Response<Map<String, String>> {
        return remoteDataSource.putUser(auth, user)
    }

    suspend fun uploadImage(image: Uri?): String? {
        return if (image != null) {
            val location = "Profile images: $currentTime: $image"
            val imageRef = storage.getReference(location)
            var downloadUri: Uri? = null
            imageRef.putFile(image)
                .await()
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                }
            }.await()
            downloadUri.toString()
        } else {
            null
        }
    }
}