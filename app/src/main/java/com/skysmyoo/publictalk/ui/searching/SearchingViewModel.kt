package com.skysmyoo.publictalk.ui.searching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchingViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _notExistUserEvent = MutableLiveData<Event<Unit>>()
    val notExistUserEvent: LiveData<Event<Unit>> = _notExistUserEvent
    private val _foundUser = MutableLiveData<Event<User>>()
    val foundUser: LiveData<Event<User>> = _foundUser

    val searchingTargetEmail = MutableLiveData<String>()

    fun searchingFriend() {
        viewModelScope.launch {
            if (!searchingTargetEmail.value.isNullOrEmpty()) {
                val foundUser = repository.findFriend(searchingTargetEmail.value ?: "")
                if (foundUser == null) {
                    _notExistUserEvent.value = Event(Unit)
                } else {
                    _foundUser.value = Event(foundUser)
                }
            }
        }
    }
}