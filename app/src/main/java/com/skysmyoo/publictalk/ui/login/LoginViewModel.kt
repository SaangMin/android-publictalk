package com.skysmyoo.publictalk.ui.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.token
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.user
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.launch

class LoginViewModel(
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
    private val _isExistUser = MutableLiveData(false)
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
        user?.let {
            it.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        if (idToken != null) {
                            viewModelScope.launch {
                                _isLoading.value = true
                                val profileImage = repository.uploadImage(imageUri)
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
                                repository.putUser(idToken, user).run {
                                    if (this.isSuccessful) {
                                        preferencesManager.saveMyEmail(it.email ?: "")
                                        repository.clearUser()
                                        repository.insertUser(user)
                                        preferencesManager.saveMyEmail(user.userEmail)
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

    fun validateExistUser(email: String?) {
        val ref = Firebase.database(BuildConfig.BASE_URL).getReference("users")
        ref.orderByChild("userEmail").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        viewModelScope.launch {
                            val user = snapshot.children.firstOrNull()?.getValue(User::class.java)
                                ?: return@launch
                            user.userDeviceToken = token ?: return@launch
                            repository.insertUser(user)
                            _isExistUser.value = true
                        }
                    } else {
                        _isExistUser.value = false
                        _googleLoginEvent.value = Unit
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "LoadUser:onCancelled: $error")
                    _googleLoginEvent.value = Unit
                }
            })
    }

    companion object {
        private const val TAG = "UserViewModel"

        fun provideFactory(repository: UserRepository) =
            viewModelFactory {
                initializer {
                    LoginViewModel(repository)
                }
            }
    }
}