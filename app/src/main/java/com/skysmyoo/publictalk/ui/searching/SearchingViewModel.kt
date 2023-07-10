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
    private val _foundUser = MutableLiveData<User>()
    val foundUser: LiveData<User> = _foundUser
    private val _alreadyFriendEvent = MutableLiveData<Event<Unit>>()
    val alreadyFriendEvent: LiveData<Event<Unit>> = _alreadyFriendEvent
    private val _addFriendEvent = MutableLiveData<Event<Unit>>()
    val addFriendEvent: LiveData<Event<Unit>> = _addFriendEvent

    val searchingTargetEmail = MutableLiveData<String>()

    fun searchingFriend() {
        viewModelScope.launch {
            if (!searchingTargetEmail.value.isNullOrEmpty()) {
                val foundUser = repository.searchFriendFromRemote(searchingTargetEmail.value ?: "")
                if (foundUser == null) {
                    _notExistUserEvent.value = Event(Unit)
                } else {
                    _foundUser.value = foundUser ?: return@launch
                }
            }
        }
    }

    fun addFriendButtonClick() {
        if(foundUser.value == null) return
        viewModelScope.launch {
            val friendEmailList = repository.getFriends().map { it.userEmail }
            if(friendEmailList.contains(searchingTargetEmail.value)) {
                _alreadyFriendEvent.value = Event(Unit)
            } else {
                val myInfo = repository.getMyInfo() ?: return@launch
                repository.addFriend(myInfo, foundUser.value ?: return@launch)
                _addFriendEvent.value = Event(Unit)
            }
        }
    }
}