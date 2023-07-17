package com.skysmyoo.publictalk.ui.chat_room

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.FirebaseDatabase
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.ChatRepository
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import com.skysmyoo.publictalk.utils.Constants.PATH_IS_CHATTING
import com.skysmyoo.publictalk.utils.Constants.PATH_MEMBER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatRoomUiState(
    val isGetChatRoomKey: Boolean = false,
    val otherUser: User? = null,
    val isFirebaseError: Boolean = false,
    val isNetworkError: Boolean = false,
)

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _isSuccessDeleteChat = MutableStateFlow(false)
    val isSuccessDeleteChat: StateFlow<Boolean> = _isSuccessDeleteChat
    private val _isFailedDeleteChat = MutableStateFlow(false)
    val isFailedDeleteChat: StateFlow<Boolean> = _isFailedDeleteChat
    private val _isCancelClick = MutableStateFlow(false)
    val isCancelClick: StateFlow<Boolean> = _isCancelClick

    private val _adapterItemList = MutableStateFlow<List<MessageBox>>(emptyList())
    val adapterItemList: StateFlow<List<MessageBox>> = _adapterItemList

    private val _chatRoomUiState = MutableStateFlow(ChatRoomUiState())
    val chatRoomUiState: StateFlow<ChatRoomUiState> = _chatRoomUiState

    val messageBody = MutableLiveData<String>()
    var currentChatRoomKey = ""

    fun getMyEmail(): String {
        return userRepository.getMyEmail() ?: ""
    }

    fun messageListener(chatRoom: ChatRoom) {
        val myEmail = getMyEmail()
        viewModelScope.launch {
            chatRepository.getMessages(chatRoom).collect { message ->
                val messageBox = if (message.sender == myEmail) {
                    MessageBox.SenderMessageBox(message)
                } else {
                    MessageBox.ReceiverMessageBox(message)
                }
                if (!_adapterItemList.value.contains(messageBox)) {
                    _adapterItemList.value = _adapterItemList.value + messageBox
                }
            }
        }
    }

    fun deleteChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            when (chatRepository.deleteChatRoom(chatRoom)) {
                is ApiException -> {
                    _isFailedDeleteChat.value = true
                    _isFailedDeleteChat.value = false
                }

                else -> {
                    _isSuccessDeleteChat.value = true
                }
            }
        }
    }

    fun onCancelClick() {
        _isCancelClick.value = true
        _isCancelClick.value = false
    }

    fun findFriend(chatRoom: ChatRoom) {
        val chatMember = chatRoom.member
        val otherUserEmail = chatMember.map { it.userEmail }.find { it != getMyEmail() } ?: ""
        viewModelScope.launch {
            val friend = userRepository.findFriend(otherUserEmail)
            _chatRoomUiState.value = _chatRoomUiState.value.copy(otherUser = friend)
        }
    }

    fun sendMessage(chatRoom: ChatRoom, showEmptyMessageToast: () -> Unit) {
        viewModelScope.launch {
            if (messageBody.value.isNullOrEmpty()) {
                showEmptyMessageToast()
            } else {
                chatRepository.createNewMessage(
                    chatRoom,
                    currentChatRoomKey,
                    messageBody.value.toString()
                ) {
                    FirebaseData.getIdToken({ token ->
                        viewModelScope.launch {
                            chatRepository.sendMessage(token, it, currentChatRoomKey)
                        }
                    }, {
                        _chatRoomUiState.value = _chatRoomUiState.value.copy(isFirebaseError = true)
                        _chatRoomUiState.value =
                            _chatRoomUiState.value.copy(isFirebaseError = false)
                    })
                }
            }
        }
    }

    fun getRoomKey(chatRoom: ChatRoom) {
        viewModelScope.launch {
            val chatRoomKey = chatRepository.getRoomKey(chatRoom.member.map { it.userEmail })
            if (chatRoomKey != null) {
                currentChatRoomKey = chatRoomKey
                _chatRoomUiState.value = _chatRoomUiState.value.copy(isGetChatRoomKey = true)
            } else {
                _chatRoomUiState.value = _chatRoomUiState.value.copy(isNetworkError = true)
                _chatRoomUiState.value = _chatRoomUiState.value.copy(isNetworkError = false)
            }
        }
    }

    fun enterChatting(chatRoom: ChatRoom) {
        val roomKey = currentChatRoomKey
        val myIdKey =
            chatRoom.member.indexOfFirst { member -> member.userEmail == getMyEmail() }
                .toString()
        chatRepository.enterChatting(roomKey, myIdKey)
    }

    fun setIsNotChatting(chatRoom: ChatRoom) {
        val roomKey = currentChatRoomKey
        val myIdKey =
            chatRoom.member.indexOfFirst { member -> member.userEmail == getMyEmail() }
                .toString()
        val chatRoomRef =
            FirebaseDatabase.getInstance().getReference(PATH_CHAT_ROOMS).child(roomKey)
                .child(PATH_MEMBER).child(myIdKey)

        chatRoomRef.child(PATH_IS_CHATTING).removeValue()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}