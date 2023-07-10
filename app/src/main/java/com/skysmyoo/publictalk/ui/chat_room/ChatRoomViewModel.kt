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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _adapterItemList = MutableLiveData<Event<List<MessageBox>>>()
    val adapterItemList: LiveData<Event<List<MessageBox>>> = _adapterItemList
    private val _newMessage = MutableLiveData<Event<MessageBox>>()
    val newMessage: LiveData<Event<MessageBox>> = _newMessage
    private val _friendData = MutableLiveData<Event<User>>()
    val friendData: LiveData<Event<User>> = _friendData
    private val _chatRoomKey = MutableLiveData<String>()
    val chatRoomKey: LiveData<String> = _chatRoomKey

    val messageBody = MutableLiveData<String>()

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
            val friend = userRepository.findFriend(otherUserEmail) ?: return@launch
            _friendData.value = Event(friend)
        }
    }

    fun sendMessage(chatRoom: ChatRoom, showEmptyMessageToast: () -> Unit) {
        if (messageBody.value.isNullOrEmpty()) {
            showEmptyMessageToast()
        } else {
            chatRepository.createNewMessage(
                chatRoom,
                chatRoomKey.value.toString(),
                messageBody.value.toString()
            ) {
                FirebaseData.getIdToken { token ->
                    viewModelScope.launch {
                        chatRepository.sendMessage(token, it, chatRoomKey.value)
                    }
                }
            }
        }
    }

    fun getRoomKey(chatRoom: ChatRoom) {
        viewModelScope.launch {
            val chatRoomKey =
                chatRepository.getRoomKey(chatRoom.member.map { it.userEmail }) ?: return@launch
            _chatRoomKey.value = chatRoomKey
        }
    }

    fun listenForChat(chatRoom: ChatRoom, roomKey: String) {
        viewModelScope.launch {
            chatRepository.listenForChat(roomKey) {
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

    fun updateIsReadingForMessages() {
        chatRepository.updateIsReadingForMessages(chatRoomKey.value.toString())
    }

    fun enterChatting(chatRoom: ChatRoom) {
        val roomKey = chatRoomKey.value ?: return
        val myIdKey =
            chatRoom.member.indexOfFirst { member -> member.userEmail == getMyEmail() }
                .toString()
        val chatRoomRef =
            FirebaseDatabase.getInstance().getReference(PATH_CHAT_ROOMS).child(roomKey)
                .child(PATH_MEMBER).child(myIdKey)

        chatRoomRef.child(PATH_IS_CHATTING).setValue(true)
        chatRoomRef.child(PATH_IS_CHATTING).onDisconnect().setValue(false)
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}