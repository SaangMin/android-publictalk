package com.skysmyoo.publictalk.ui.chat_room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _adapterItemList = MutableLiveData<Event<List<MessageBox>>>()
    val adapterItemList: LiveData<Event<List<MessageBox>>> = _adapterItemList

    fun getMyEmail(): String {
        return repository.getMyEmail() ?: ""
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
}