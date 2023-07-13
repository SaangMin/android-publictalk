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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class FriendListUiState(
    val adapterItemList: List<FriendListScreenData> = emptyList(),
    val isFriendClicked: Boolean = false,
    val isMyInfoClicked: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _chatRoomClickEvent = MutableLiveData<Event<Unit>>()
    val chatRoomClickEvent: LiveData<Event<Unit>> = _chatRoomClickEvent
    private val _chatRoomList = MutableLiveData<Event<List<ChatRoom>>>()
    val chatRoomList: LiveData<Event<List<ChatRoom>>> = _chatRoomList
    private val _notExistChatRoom = MutableLiveData<Event<Unit>>()
    val notExistChatRoom: LiveData<Event<Unit>> = _notExistChatRoom
    private val _foundChatRoom = MutableLiveData<Event<ChatRoom>>()
    val foundChatRoom: LiveData<Event<ChatRoom>> = _foundChatRoom
    private val _removeFriendEvent = MutableLiveData<Event<Unit>>()
    val removeFriendEvent: LiveData<Event<Unit>> = _removeFriendEvent
    private val _networkErrorEvent = MutableLiveData<Event<Unit>>()
    val networkErrorEvent: LiveData<Event<Unit>> = _networkErrorEvent

    private val _friendListUiState = MutableStateFlow(FriendListUiState())
    val friendListUiState: StateFlow<FriendListUiState> = _friendListUiState

    var clickedChatRoom: ChatRoom? = null
    var clickedFriend: User? = null

    fun setAdapterItemList(textOfMe: String, textOfFriend: String) {
        viewModelScope.launch {
            val myInfo = repository.getMyInfo() ?: return@launch
            val friendList = repository.getFriends()
            val itemList = mutableListOf(
                FriendListScreenData.Header(textOfMe),
                FriendListScreenData.Friend(myInfo),
                FriendListScreenData.Header(textOfFriend)
            )

            if (friendList.isNotEmpty()) {
                val friendListScreenData = friendList.map { FriendListScreenData.Friend(it) }
                itemList.addAll(friendListScreenData)
                _friendListUiState.value = _friendListUiState.value.copy(adapterItemList = itemList)
            } else {
                _friendListUiState.value = _friendListUiState.value.copy(adapterItemList = itemList)
            }
        }
    }

    fun getOtherUser(chatRoom: ChatRoom): User? {
        val friendList = runBlocking { repository.getFriends() }
        val otherUserEmail = chatRoom.member.map { it.userEmail }.find { it != getMyEmail() }
        return friendList.find { it.userEmail == otherUserEmail }
    }

    fun getChatRooms() {
        viewModelScope.launch {
            val localChatRoom = repository.getChatRooms()
            _chatRoomList.value = Event(localChatRoom)
            val myEmail = getMyEmail()
            FirebaseData.getIdToken({
                viewModelScope.launch {
                    val chatRoomList =
                        repository.updateChatRooms(it, myEmail)
                    _chatRoomList.value = Event(chatRoomList)
                }
            }, {
                viewModelScope.launch {
                    val chatRoomList = repository.getChatRooms()
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
            _friendListUiState.value = _friendListUiState.value.copy(isMyInfoClicked = true)
            _friendListUiState.value = _friendListUiState.value.copy(isMyInfoClicked = false)
        } else {
            clickedFriend = friend
            _friendListUiState.value = _friendListUiState.value.copy(isFriendClicked = true)
            _friendListUiState.value = _friendListUiState.value.copy(isFriendClicked = false)
        }
    }

    fun getChatRoom(member: List<String>) {
        viewModelScope.launch {
            val chatRoom = repository.getChatRoom(member)
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