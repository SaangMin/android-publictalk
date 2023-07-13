package com.skysmyoo.publictalk.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _adapterItemList = MutableLiveData<Event<List<FriendListScreenData>>>()
    val adapterItemList: LiveData<Event<List<FriendListScreenData>>> = _adapterItemList
    private val _chatRoomClickEvent = MutableLiveData<Event<Unit>>()
    val chatRoomClickEvent: LiveData<Event<Unit>> = _chatRoomClickEvent
    private val _chatRoomList = MutableLiveData<Event<List<ChatRoom>>>()
    val chatRoomList: LiveData<Event<List<ChatRoom>>> = _chatRoomList
    private val _notExistChatRoom = MutableLiveData<Event<Unit>>()
    val notExistChatRoom: LiveData<Event<Unit>> = _notExistChatRoom
    private val _foundChatRoom = MutableLiveData<Event<ChatRoom>>()
    val foundChatRoom: LiveData<Event<ChatRoom>> = _foundChatRoom
    private val _friendClickEvent = MutableLiveData<Event<Unit>>()
    val friendClickEvent: LiveData<Event<Unit>> = _friendClickEvent
    private val _myInfoClickEvent = MutableLiveData<Event<Unit>>()
    val myInfoClickEvent: LiveData<Event<Unit>> = _myInfoClickEvent
    private val _removeFriendEvent = MutableLiveData<Event<Unit>>()
    val removeFriendEvent: LiveData<Event<Unit>> = _removeFriendEvent
    private val _networkErrorEvent = MutableLiveData<Event<Unit>>()
    val networkErrorEvent: LiveData<Event<Unit>> = _networkErrorEvent

    var clickedChatRoom: ChatRoom? = null
    var clickedFriend: User? = null

    fun setAdapterItemList(textOfMe: String, textOfFriend: String) {
        viewModelScope.launch {
            val myInfo = repository.getMyInfo() ?: return@launch
            val friendList = repository.getFriends().stateIn(viewModelScope).value
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

    fun getOtherUser(chatRoom: ChatRoom): User? {
        val friendList = runBlocking { repository.getFriends().stateIn(viewModelScope).value }
        val otherUserEmail = chatRoom.member.map { it.userEmail }.find { it != getMyEmail() }
        return friendList.find { it.userEmail == otherUserEmail }
    }

    fun getChatRooms() {
        viewModelScope.launch {
            val localChatRoom = repository.getChatRooms().stateIn(viewModelScope).value
            _chatRoomList.value = Event(localChatRoom)
            val myEmail = getMyEmail()
            FirebaseData.getIdToken({
                viewModelScope.launch {
                    val chatRoomList =
                        repository.updateChatRooms(it, myEmail).stateIn(viewModelScope).value
                    _chatRoomList.value = Event(chatRoomList)
                }
            }, {
                viewModelScope.launch {
                    val chatRoomList = repository.getChatRooms().stateIn(viewModelScope).value
                    _chatRoomList.value = Event(chatRoomList)
                }
            })
        }
    }

    fun getMyEmail(): String {
        return repository.getMyEmail() ?: ""
    }

    fun onClickChatRoom(chatRoom: ChatRoom) {
        clickedChatRoom = chatRoom
        _chatRoomClickEvent.value = Event(Unit)
    }

    fun onClickFriend(friend: User) {
        if (friend.userEmail == getMyEmail()) {
            _myInfoClickEvent.value = Event(Unit)
        } else {
            clickedFriend = friend
            _friendClickEvent.value = Event(Unit)
        }
    }

    fun getChatRoom(member: List<String>) {
        viewModelScope.launch {
            val chatRoom = repository.getChatRoom(member).stateIn(viewModelScope).value
            if (chatRoom == null) {
                _notExistChatRoom.value = Event(Unit)
            } else {
                _foundChatRoom.value = Event(chatRoom)
            }
        }
    }

    fun removeFriend(friend: User) {
        viewModelScope.launch {
            val myInfo = repository.getMyInfo() ?: return@launch
            val response = repository.removeFriend(myInfo, friend)
            if (response is ApiResultSuccess) {
                _removeFriendEvent.value = Event(Unit)
            } else {
                _networkErrorEvent.value = Event(Unit)
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}