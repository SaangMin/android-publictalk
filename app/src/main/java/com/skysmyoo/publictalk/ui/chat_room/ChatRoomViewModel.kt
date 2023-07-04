package com.skysmyoo.publictalk.ui.chat_room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.ChatRepository
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.utils.Event
import com.skysmyoo.publictalk.utils.TimeUtil
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
    private val _messageList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>> = _messageList

    private val currentTime = TimeUtil.getCurrentDateString()

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

    fun sendMessage(chatRoom: ChatRoom, messageBody: String) {
        val message = Message(chatRoom.me, chatRoom.other?.userEmail ?: "", messageBody, false, currentTime)
        FirebaseData.getIdToken {
            viewModelScope.launch {
                Log.d(TAG,"${chatRepository.getCurrentRoom(chatRoom.me, chatRoom.other?.userEmail!!)}")
                val messageList = chatRepository.sendMessage(it, chatRoom.me, chatRoom.other!!, message)
                Log.d(TAG,"$messageList")
            }
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}