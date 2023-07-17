package com.skysmyoo.publictalk.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class FriendListUiState(
    val adapterItemList: List<FriendListScreenData> = emptyList(),
    val isFriendClicked: Boolean = false,
    val isMyInfoClicked: Boolean = false,
)

data class ChatListUiState(
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

    val chatRoomList: StateFlow<List<ChatRoom>> = transformChatList().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    var clickedChatRoom: ChatRoom? = null
    var clickedFriend: User? = null
    var foundChatRoom: ChatRoom? = null
    var createdNewChatRoom: ChatRoom? = null

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

    private fun transformChatList(): Flow<List<ChatRoom>> = repository.getRemoteChatRooms().map {
        it.sortedByDescending { chatRoom ->
            chatRoom.messages.values.lastOrNull()?.createdAt ?: chatRoom.chatCreatedAt
        }
    }

    fun getOtherUser(chatRoom: ChatRoom): User? {
        val friendList = runBlocking { repository.getFriends() }
        val otherUserEmail = chatRoom.member.map { it.userEmail }.find { it != getMyEmail() }
        return friendList.find { it.userEmail == otherUserEmail }
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
                val friendEmail = member.find { it != getMyEmail() } ?: ""
                val chatRoomMember = listOf(
                    ChattingMember(userEmail = getMyEmail()),
                    ChattingMember(userEmail = friendEmail)
                )
                val newChatRoom = ChatRoom(
                    member = chatRoomMember,
                    chatCreatedAt = TimeUtil.getCurrentDateString()
                )
                val createdChatRoom = repository.createChatRoom(newChatRoom)
                createdNewChatRoom = createdChatRoom
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNotExistChatRoom = true)
                delay(1000)
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