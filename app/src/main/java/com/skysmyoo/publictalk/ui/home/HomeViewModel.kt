package com.skysmyoo.publictalk.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
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

data class ChatListUiState(
    val chatRoomList: List<ChatRoom> = emptyList(),
    val isChatRoomClicked: Boolean = false,
)

data class FriendInfoUiState(
    val isNotExistChatRoom: Boolean = false,
    val isFoundChatRoom: Boolean = false,
    val isRemovedFriend: Boolean = false,
    val isNetworkError: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _friendListUiState = MutableStateFlow(FriendListUiState())
    val friendListUiState: StateFlow<FriendListUiState> = _friendListUiState

    private val _chatListUiState = MutableStateFlow(ChatListUiState())
    val chatListUiState: StateFlow<ChatListUiState> = _chatListUiState

    private val _friendInfoUiState = MutableStateFlow(FriendInfoUiState())
    val friendInfoUiState: StateFlow<FriendInfoUiState> = _friendInfoUiState

    var clickedChatRoom: ChatRoom? = null
    var clickedFriend: User? = null
    var foundChatRoom: ChatRoom? = null

    fun setAdapterItemList(textOfMe: String, textOfFriend: String) {
        viewModelScope.launch {
            val myInfo = repository.getMyInfo() ?: return@launch
            val friendList = repository.getFriends().sortedByDescending { it.userName }
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
            _chatListUiState.value = _chatListUiState.value.copy(chatRoomList = localChatRoom)
            val myEmail = getMyEmail()
            FirebaseData.getIdToken({
                viewModelScope.launch {
                    val chatRoomList =
                        repository.updateChatRooms(it, myEmail)
                    _chatListUiState.value = _chatListUiState.value.copy(chatRoomList = chatRoomList)
                }
            }, {
                viewModelScope.launch {
                    val chatRoomList = repository.getChatRooms()
                    _chatListUiState.value = _chatListUiState.value.copy(chatRoomList = chatRoomList)
                }
            })
        }
    }

    fun getMyEmail(): String {
        return repository.getMyEmail() ?: ""
    }

    fun onClickChatRoom(chatRoom: ChatRoom) {
        clickedChatRoom = chatRoom
        _chatListUiState.value = _chatListUiState.value.copy(isChatRoomClicked = true)
        _chatListUiState.value = _chatListUiState.value.copy(isChatRoomClicked = false)
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
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNotExistChatRoom = true)
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNotExistChatRoom = false)
            } else {
                foundChatRoom = chatRoom
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isFoundChatRoom = true)
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isFoundChatRoom = false)
            }
        }
    }

    fun removeFriend(friend: User) {
        viewModelScope.launch {
            val myInfo = repository.getMyInfo() ?: return@launch
            val response = repository.removeFriend(myInfo, friend)
            if (response is ApiResultSuccess) {
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isRemovedFriend = true)
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isRemovedFriend = false)
            } else {
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNetworkError = true)
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNetworkError = false)
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}