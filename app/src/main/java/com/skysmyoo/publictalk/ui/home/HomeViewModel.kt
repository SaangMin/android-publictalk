package com.skysmyoo.publictalk.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _adapterItemList = MutableLiveData<Event<List<FriendListScreenData>>>()
    val adapterItemList: LiveData<Event<List<FriendListScreenData>>> = _adapterItemList
    private val _chatRoomClickEvent = MutableLiveData<Event<Unit>>()
    val chatRoomClickEvent: LiveData<Event<Unit>> = _chatRoomClickEvent

    var clickedChatRoom: ChatRoom? = null

    fun setAdapterItemList(textOfMe: String, textOfFriend: String) {
        viewModelScope.launch {
            val myEmail = repository.getMyEmail() ?: return@launch
            val myInfo = repository.getMyInfo(myEmail) ?: return@launch
            val friendList = repository.getFriends()
            val itemList = mutableListOf(
                FriendListScreenData.Header(textOfMe),
                FriendListScreenData.Friend(myInfo),
                FriendListScreenData.Header(textOfFriend)
            )

            if (friendList.isNotEmpty()) {
                val friendListScreenData = friendList.map { FriendListScreenData.Friend(it) }
                itemList.addAll(friendListScreenData)
                _adapterItemList.value = Event(itemList)
            } else {
                _adapterItemList.value = Event(itemList)
            }
        }
    }

    fun getMyEmail(): String {
        return repository.getMyEmail() ?: ""
    }

    fun onClickChatRoom(chatRoom: ChatRoom) {
        clickedChatRoom = chatRoom
        _chatRoomClickEvent.value = Event(Unit)
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}