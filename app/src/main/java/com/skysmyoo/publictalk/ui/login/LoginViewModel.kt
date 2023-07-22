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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetInfoUiState(
    val isLoading: Boolean = false,
    val isFailed: Boolean = false,
    val isImageClicked: Boolean = false,
    val isSubmit: Boolean = false,
    val isNotRequired: Boolean = false,
)

data class LoginUiState(
    val isGoogleLogin: Boolean = false,
    val isExist: Boolean? = null,
)

data class SplashUiState(
    val isExist: Boolean? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val _splashUiState = MutableStateFlow(SplashUiState())
    val splashUiState: StateFlow<SplashUiState> = _splashUiState

    private val _setInfoUiState = MutableStateFlow(SetInfoUiState())
    val setInfoUiState: StateFlow<SetInfoUiState> = _setInfoUiState

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    val name = MutableLiveData("")
    val phoneNumber = MutableLiveData("")

    fun onSubmitClick() {
        if (isEmptyContent()) {
            _setInfoUiState.value = _setInfoUiState.value.copy(isNotRequired = true)
            _setInfoUiState.value = _setInfoUiState.value.copy(isNotRequired = false)
        } else {
            _setInfoUiState.value = _setInfoUiState.value.copy(isSubmit = true)
            _setInfoUiState.value = _setInfoUiState.value.copy(isSubmit = false)
        }
    }

    fun addImageClick() {
        _setInfoUiState.value = _setInfoUiState.value.copy(isImageClicked = true)
        _setInfoUiState.value = _setInfoUiState.value.copy(isImageClicked = false)
    }

    suspend fun submitUser(
        imageUri: Uri?, userLanguage: String,
        startHomeActivity: () -> Unit,
    ) {
        _setInfoUiState.value = _setInfoUiState.value.copy(isLoading = true)
        val profileImageFlow = repository.uploadImage(imageUri).stateIn(viewModelScope)
        val user = User(
            userEmail = FirebaseData.user?.email ?: return,
            userName = name.value ?: "",
            userPhoneNumber = phoneNumber.value ?: "",
            userProfileImage = profileImageFlow.value,
            userLanguage = userLanguage,
            userDeviceToken = token ?: "",
            userFriendIdList = emptyList(),
            userCreatedAt = TimeUtil.getCurrentDateString()
        )
        FirebaseData.getIdToken({ idToken ->
            viewModelScope.launch {
                val putUserFlow = repository.putUser(idToken, user).stateIn(viewModelScope)
                if (putUserFlow.value != null) {
                    _setInfoUiState.value = _setInfoUiState.value.copy(isFailed = false)
                    _setInfoUiState.value = _setInfoUiState.value.copy(isLoading = false)
                    startHomeActivity()
                } else {
                    _setInfoUiState.value = _setInfoUiState.value.copy(isFailed = true)
                    _setInfoUiState.value = _setInfoUiState.value.copy(isLoading = false)
                }
            }
        }, {
            _setInfoUiState.value = _setInfoUiState.value.copy(isFailed = true)
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
                        val updatedUser = user.copy(userDeviceToken = token ?: "")
                        repository.updateUser(it, updatedUser).collect()
                        repository.updateFriends(updatedUser, updatedUser.userFriendIdList).collect()
                        repository.updateChatRooms(it, updatedUser.userEmail)
                        _splashUiState.value = _splashUiState.value.copy(isExist = true)
                        _loginUiState.value = _loginUiState.value.copy(isExist = true)
                    }
                }, {
                    viewModelScope.launch {
                        val originUser = repository.getMyInfo()
                        if (originUser != null) {
                            _splashUiState.value = _splashUiState.value.copy(isExist = true)
                            _loginUiState.value = _loginUiState.value.copy(isExist = true)
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
                _splashUiState.value = _splashUiState.value.copy(isExist = true)
                _loginUiState.value = _loginUiState.value.copy(isExist = true)
            } else {
                _splashUiState.value = _splashUiState.value.copy(isExist = false)
                _loginUiState.value =
                    _loginUiState.value.copy(isGoogleLogin = true, isExist = false)
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