package com.skysmyoo.publictalk.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.local.FriendListScreenData
import com.skysmyoo.publictalk.data.model.local.Language
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class FriendListUiState(
    val isFriendClicked: Boolean = false,
    val isMyInfoClicked: Boolean = false,
    val isUpdatedFriend: Boolean = false,
)

data class ChatListUiState(
    val isChatRoomClicked: Boolean = false,
    val isChatListUpdated: Boolean = false,
)

data class SettingUiState(
    val isGettingMyInfo: Boolean = false,
    val language: Language? = null,
    val isLogoutClick: Boolean = false,
    val isImageClicked: Boolean = false,
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val isFailed: Boolean = false,
    val isDeleteAccount: Boolean = false,
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

    private val _settingUiState = MutableStateFlow(SettingUiState())
    val settingUiState: StateFlow<SettingUiState> = _settingUiState

    private val _chatRoomList = MutableStateFlow<List<ChatRoom>>(emptyList())
    var chatRoomList: StateFlow<List<ChatRoom>> = _chatRoomList

    private val _friendList = MutableStateFlow<List<FriendListScreenData>>(emptyList())
    var friendList: StateFlow<List<FriendListScreenData>> = _friendList

    var clickedChatRoom: ChatRoom? = null
    var clickedFriend: User? = null
    var foundChatRoom: ChatRoom? = null
    var createdNewChatRoom: ChatRoom? = null
    var myInfo: User? = null

    fun getMyInfo() {
        viewModelScope.launch {
            val user = repository.getMyInfo()
            myInfo = user
            _settingUiState.value = _settingUiState.value.copy(isGettingMyInfo = true)
            _settingUiState.value = _settingUiState.value.copy(
                language = Language.fromCode(
                    myInfo?.userLanguage ?: "ko"
                )
            )
        }
    }

    fun addImageClick() {
        _settingUiState.value = _settingUiState.value.copy(isImageClicked = true)
        _settingUiState.value = _settingUiState.value.copy(isImageClicked = false)
    }

    fun editUser(user: User, imageUri: Uri?, userLanguage: String) {
        viewModelScope.launch {
            _settingUiState.value = _settingUiState.value.copy(isLoading = true)
            val profileImageFlow = repository.uploadImage(imageUri).stateIn(viewModelScope)
            val editedUser = user.copy(
                userProfileImage = profileImageFlow.value ?: user.userProfileImage,
                userLanguage = userLanguage
            )
            FirebaseData.getIdToken({
                viewModelScope.launch {
                    repository.updateUser(it, editedUser).collect()
                    _settingUiState.value = _settingUiState.value.copy(isLoading = false)
                    _settingUiState.value = _settingUiState.value.copy(isEdit = true)
                }
            }, {
                Log.e(TAG, "Get token failed!")
                _settingUiState.value = _settingUiState.value.copy(isFailed = true)
                _settingUiState.value = _settingUiState.value.copy(isLoading = false)
                _settingUiState.value = _settingUiState.value.copy(isFailed = false)
            })
        }
    }

    fun logout() {
        repository.clearMyData()
        _settingUiState.value = _settingUiState.value.copy(isLogoutClick = true)
    }

    fun setAdapterItemList(textOfMe: String, textOfFriend: String) {
        viewModelScope.launch {
            val itemList =
                repository.updateFriendList(textOfMe, textOfFriend).stateIn(viewModelScope)
            _friendList.value = itemList.value
            _friendListUiState.value = _friendListUiState.value.copy(isUpdatedFriend = true)
            delay(1000)
            _friendListUiState.value = _friendListUiState.value.copy(isUpdatedFriend = false)
        }
    }

    fun refreshChatRoomList() {
        viewModelScope.launch {
            val newChatRoomList = repository.getRemoteChatRooms().stateIn(viewModelScope)
            _chatRoomList.value = newChatRoomList.value
            _chatListUiState.value = _chatListUiState.value.copy(isChatListUpdated = true)
            delay(1000)
            _chatListUiState.value = _chatListUiState.value.copy(isChatListUpdated = false)
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
                _friendListUiState.value = _friendListUiState.value.copy(isUpdatedFriend = true)
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isRemovedFriend = false)
                _friendListUiState.value = _friendListUiState.value.copy(isUpdatedFriend = false)
            } else {
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNetworkError = true)
                _friendInfoUiState.value = _friendInfoUiState.value.copy(isNetworkError = false)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _settingUiState.value = _settingUiState.value.copy(isLoading = true)
            val response = repository.deleteAccount()
            if (response is ApiResultSuccess) {
                repository.clearRoomData()
                _settingUiState.value = _settingUiState.value.copy(isDeleteAccount = true)
                _settingUiState.value = _settingUiState.value.copy(isLoading = false)
            } else {
                _settingUiState.value = _settingUiState.value.copy(isFailed = true)
                _settingUiState.value = _settingUiState.value.copy(isFailed = false)
                _settingUiState.value = _settingUiState.value.copy(isLoading = false)
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}