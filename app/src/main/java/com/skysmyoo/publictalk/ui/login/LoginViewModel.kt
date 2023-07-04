package com.skysmyoo.publictalk.ui.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.remote.Friend
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setUserInfo
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.token
import com.skysmyoo.publictalk.utils.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val _addImageEvent = MutableLiveData<Unit>()
    val addImageEvent: LiveData<Unit> = _addImageEvent
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _submitEvent = MutableLiveData<Unit>()
    val submitEvent: LiveData<Unit> = _submitEvent
    private val _notRequiredEvent = MutableLiveData<Unit>()
    val notRequiredEvent: LiveData<Unit> = _notRequiredEvent
    private val _isExistUser = MutableLiveData<Boolean>()
    val isExistUser: LiveData<Boolean> = _isExistUser
    private val _googleLoginEvent = MutableLiveData(Unit)
    val googleLoginEvent: LiveData<Unit> = _googleLoginEvent

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
        FirebaseData.getIdToken { idToken ->
            viewModelScope.launch {
                _isLoading.value = true
                val profileImage = repository.uploadImage(imageUri)
                val user = User(
                    userEmail = FirebaseData.user?.email ?: "",
                    userName = name.value ?: "",
                    userPhoneNumber = phoneNumber.value ?: "",
                    userProfileImage = profileImage,
                    userLanguage = userLanguage,
                    userDeviceToken = token ?: "",
                    userFriendIdList = listOf(Friend(userEmail = "iu@gmail.com")),
                    userCreatedAt = TimeUtil.getCurrentDateString()
                )
                repository.putUser(idToken, user).run {
                    if (this.isSuccessful) {
                        _isLoading.value = false
                        startHomeActivity()
                    } else {
                        Log.e(TAG, "put user error!: ${errorBody()}")
                    }
                }
            }
        }
    }

    private fun isEmptyContent(): Boolean {
        return name.value.isNullOrEmpty() || phoneNumber.value.isNullOrEmpty()
    }

    fun validateExistUser(email: String?) {
        viewModelScope.launch {
            val user = repository.getExistUser(email)?.values?.firstOrNull()
            if (user != null) {
                setUserInfo()
                FirebaseData.getIdToken { idToken ->
                    viewModelScope.launch {
                        repository.updateUser(idToken, user)
                        repository.updateFriends(user, user.userFriendIdList)
                        repository.updateChatRoom(user.userEmail)
                        _isExistUser.value = true
                    }
                }
            } else {
                _isExistUser.value = false
                _googleLoginEvent.value = Unit
            }
        }
    }

    fun getMyEmail(): String? {
        return repository.getMyEmail()
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}