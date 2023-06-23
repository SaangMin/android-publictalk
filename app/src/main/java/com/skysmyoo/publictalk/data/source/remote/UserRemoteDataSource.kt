package com.skysmyoo.publictalk.data.source.remote

import android.net.Uri
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class UserRemoteDataSource(private val apiClient: ApiClient) {
    suspend fun putUser(auth: String, user: User): Response<Map<String, String>> {
        return apiClient.putUser(auth, user)
    }

    suspend fun uploadImage(image: Uri?): String? {

        val currentTime = TimeUtil.getCurrentDateString()

        return if (image != null) {
            val location = "Profile images: $currentTime: $image"
            val imageRef = FirebaseData.storage.getReference(location)
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