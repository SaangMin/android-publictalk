package com.skysmyoo.publictalk.ui.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.token
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.user
import com.skysmyoo.publictalk.data.source.remote.SignInRemoteDataSource
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    private val remoteDataSource: SignInRemoteDataSource,
) : ViewModel() {

    private val _addImageEvent = MutableLiveData<Unit>()
    val addImageEvent: LiveData<Unit> = _addImageEvent
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _submitEvent = MutableLiveData<Unit>()
    val submitEvent: LiveData<Unit> = _submitEvent
    private val _notRequiredEvent = MutableLiveData<Unit>()
    val notRequiredEvent: LiveData<Unit> = _notRequiredEvent

    val name = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()

    fun onSubmitClick() {
        if (isEmptyContent()) {
            _notRequiredEvent.value = Unit
        } else {
            _submitEvent.value = Unit
        }
    }

    fun addImageClick() {
        _addImageEvent.value = Unit
    }

    fun submitUser(
        imageUri: Uri?,
        userLanguage: String,
        startHomeActivity: () -> Unit,
    ) {
        user?.let {
            it.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        if (idToken != null) {
                            viewModelScope.launch {
                                _isLoading.value = true
                                val profileImage = remoteDataSource.uploadImage(imageUri)
                                val user = User(
                                    userEmail = it.email ?: "",
                                    userName = name.value ?: "",
                                    userPhoneNumber = phoneNumber.value ?: "",
                                    userProfileImage = profileImage,
                                    userLanguage = userLanguage,
                                    userDeviceToken = token ?: "",
                                    userFriendIdList = null,
                                    userCreatedAt = TimeUtil.getCurrentDateString()
                                )
                                remoteDataSource.putUser(idToken, user).run {
                                    if (this.isSuccessful) {
                                        repository.insertUser(user)
                                        _isLoading.value = false
                                        startHomeActivity()
                                    } else {
                                        Log.e(TAG, "put user error!: ${errorBody()}")
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "idToken is null")
                        }
                    } else {
                        Log.e(TAG, "put user error!")
                    }
                }
        }
    }

    private fun isEmptyContent(): Boolean {
        return name.value.isNullOrEmpty() || phoneNumber.value.isNullOrEmpty()
    }

    companion object {
        private const val TAG = "UserViewModel"

        fun provideFactory(repository: UserRepository, remoteDataSource: SignInRemoteDataSource) =
            viewModelFactory {
                initializer {
                    UserViewModel(repository, remoteDataSource)
                }
            }
    }
}