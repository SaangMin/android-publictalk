package com.skysmyoo.publictalk.ui.searching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultError
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchingViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _notExistUserEvent = MutableLiveData<Event<Unit>>()
    val notExistUserEvent: LiveData<Event<Unit>> = _notExistUserEvent
    private val _foundUser = MutableLiveData<User>()
    val foundUser: LiveData<User> = _foundUser
    private val _alreadyFriendEvent = MutableLiveData<Event<Unit>>()
    val alreadyFriendEvent: LiveData<Event<Unit>> = _alreadyFriendEvent
    private val _addFriendEvent = MutableLiveData<Event<Unit>>()
    val addFriendEvent: LiveData<Event<Unit>> = _addFriendEvent
    private val _networkErrorEvent = MutableLiveData<Event<Unit>>()
    val networkErrorEvent: LiveData<Event<Unit>> = _networkErrorEvent
    private val _addFriendFailEvent = MutableLiveData<Event<Unit>>()
    val addFriendFailEvent: LiveData<Event<Unit>> = _addFriendFailEvent

    val searchingTargetEmail = MutableLiveData<String>()

    fun searchingFriend() {
        viewModelScope.launch {
            if (!searchingTargetEmail.value.isNullOrEmpty()) {
                when (val foundUser =
                    repository.searchFriendFromRemote(searchingTargetEmail.value ?: "")) {
                    is ApiResultSuccess -> {
                        _foundUser.value = foundUser.data
                    }

                    is ApiResultError -> {
                        _networkErrorEvent.value = Event(Unit)
                    }

                    else -> {
                        _notExistUserEvent.value = Event(Unit)
                    }
                }
            }
        }
    }

    fun addFriendButtonClick() {
        if (foundUser.value == null) return
        viewModelScope.launch {
            val friendEmailList =
                repository.getFriends().stateIn(viewModelScope).value.map { it.userEmail }
            if (friendEmailList.contains(searchingTargetEmail.value)) {
                _alreadyFriendEvent.value = Event(Unit)
            } else {
                val myInfo = repository.getMyInfo() ?: return@launch
                val response = repository.addFriend(myInfo, foundUser.value ?: return@launch)
                if (response is ApiResultSuccess) {
                    _addFriendEvent.value = Event(Unit)
                } else {
                    _addFriendFailEvent.value = Event(Unit)
                }
            }
        }
    }
}