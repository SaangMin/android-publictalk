package com.skysmyoo.publictalk.ui.login

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setUserInfo
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.token
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val _addImageEvent = MutableStateFlow(Unit)
    val addImageEvent: StateFlow<Unit> = _addImageEvent
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _submitEvent = MutableStateFlow(Unit)
    val submitEvent: StateFlow<Unit> = _submitEvent
    private val _notRequiredEvent = MutableStateFlow(Unit)
    val notRequiredEvent: StateFlow<Unit> = _notRequiredEvent
    private val _isExistUser = MutableSharedFlow<Boolean>()
    val isExistUser: SharedFlow<Boolean> = _isExistUser
    private val _googleLoginEvent = MutableStateFlow(Unit)
    val googleLoginEvent: StateFlow<Unit> = _googleLoginEvent
    private val _failedMessage = MutableStateFlow(Unit)
    val failedMessage: StateFlow<Unit> = _failedMessage

    val name = MutableLiveData("")
    val phoneNumber = MutableLiveData("")

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
        FirebaseData.getIdToken({ idToken ->
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
                    userFriendIdList = listOf("iu@gmail.com"),
                    userCreatedAt = TimeUtil.getCurrentDateString()
                )
                repository.putUser(idToken, user).run {
                    if (this != null) {
                        _isLoading.value = false
                        startHomeActivity()
                    } else {
                        _isLoading.value = false
                        _failedMessage.value = Unit
                    }
                }
            }
        }, {
            _failedMessage.value = Unit
        })
    }

    private fun isEmptyContent(): Boolean {
        return name.value.isNullOrEmpty() || phoneNumber.value.isNullOrEmpty()
    }

    fun validateExistUser(email: String?) {
        viewModelScope.launch {
            val response = repository.getExistUser(email)
            if (response is ApiResultSuccess) {
                val user = response.data.values.first()
                setUserInfo()
                FirebaseData.getIdToken({
                    viewModelScope.launch {
                        repository.updateUser(it, user)
                        repository.updateFriends(user, user.userFriendIdList)
                        repository.updateChatRooms(it, user.userEmail)
                        _isExistUser.emit(true)
                    }
                }, {
                    viewModelScope.launch {
                        val originUser = repository.getMyInfo()
                        if (originUser != null) {
                            _isExistUser.emit(true)
                        }
                    }
                })
            } else {
                localLogin()
            }
        }
    }

    fun localLogin() {
        viewModelScope.launch {
            val user = repository.getMyInfo()
            if (user != null) {
                _isExistUser.emit(true)
            } else {
                _isExistUser.emit(false)
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