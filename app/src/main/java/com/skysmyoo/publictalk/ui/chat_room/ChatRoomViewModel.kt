package com.skysmyoo.publictalk.ui.chat_room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.ChatRepository
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import com.skysmyoo.publictalk.utils.Constants.PATH_IS_CHATTING
import com.skysmyoo.publictalk.utils.Constants.PATH_MEMBER
import com.skysmyoo.publictalk.utils.Event
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

    private val _adapterItemList = MutableLiveData<Event<List<MessageBox>>>()
    val adapterItemList: LiveData<Event<List<MessageBox>>> = _adapterItemList
    private val _newMessage = MutableLiveData<Event<MessageBox>>()
    val newMessage: LiveData<Event<MessageBox>> = _newMessage

    private val _chatRoomUiState = MutableStateFlow(ChatRoomUiState())
    val chatRoomUiState: StateFlow<ChatRoomUiState> = _chatRoomUiState

    val messageBody = MutableLiveData<String>()
    var currentChatRoomKey = ""
    var foundFriend: User? = null

    fun getMyEmail(): String {
        return userRepository.getMyEmail() ?: ""
    }

    fun setAdapterItemList(messageList: List<Message>) {
        val myEmail = getMyEmail()
        val resultMessageList = mutableListOf<MessageBox>()
        messageList.sortedBy { it.createdAt }.forEach {
            if (it.sender == myEmail) {
                resultMessageList.add(MessageBox.SenderMessageBox(it))
            } else {
                resultMessageList.add(MessageBox.ReceiverMessageBox(it))
            }
        }
        _adapterItemList.value = Event(resultMessageList)
    }

    fun findFriend(chatRoom: ChatRoom) {
        val chatMember = chatRoom.member
        val otherUserEmail = chatMember.map { it.userEmail }.find { it != getMyEmail() } ?: ""
        viewModelScope.launch {
            val friend = userRepository.findFriend(otherUserEmail)
            foundFriend = friend
            _chatRoomUiState.value = _chatRoomUiState.value.copy(otherUser = friend)
        }
    }

    fun sendMessage(chatRoom: ChatRoom, showEmptyMessageToast: () -> Unit) {
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
                    _chatRoomUiState.value = _chatRoomUiState.value.copy(isFirebaseError = false)
                })
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

    fun listenForChat(chatRoom: ChatRoom, roomKey: String) {
        viewModelScope.launch {
            chatRepository.chatListener(roomKey) {
                if (!chatRoom.messages.values.toList().contains(it)) {
                    val newMessage = convertToMessage(it)
                    _newMessage.value = Event(newMessage)
                }
            }
        }
    }

    private fun convertToMessage(message: Message): MessageBox {
        return if (message.sender == getMyEmail()) {
            MessageBox.SenderMessageBox(message)
        } else {
            MessageBox.ReceiverMessageBox(message)
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

        chatRoomRef.child(PATH_IS_CHATTING).setValue(false)
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}