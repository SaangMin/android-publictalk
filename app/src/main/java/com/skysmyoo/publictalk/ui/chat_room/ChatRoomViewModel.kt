package com.skysmyoo.publictalk.ui.chat_room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysmyoo.publictalk.data.model.local.MessageBox
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.model.remote.User
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
    private val _newMessage = MutableLiveData<Event<Message>>()
    val newMessage: LiveData<Event<Message>> = _newMessage
    private val _friendData = MutableLiveData<Event<User>>()
    val friendData: LiveData<Event<User>> = _friendData
    private val _chatRoomKey = MutableLiveData<String>()
    val chatRoomKey: LiveData<String> = _chatRoomKey

    private val currentTime = TimeUtil.getCurrentDateString()
    private val database = FirebaseDatabase.getInstance()

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

    fun findFriend(email: String) {
        viewModelScope.launch {
            val friend = userRepository.findFriend(email) ?: return@launch
            _friendData.value = Event(friend)
        }
    }

    fun sendMessage(chatRoom: ChatRoom) {
        val myEmail = chatRoom.member.map { it.userEmail }.find { it == getMyEmail() } ?: ""
        val otherEmail = chatRoom.member.map { it.userEmail }.find { it != myEmail } ?: ""
        val membersRef = database.getReference("chatRooms/${chatRoomKey.value}/member")

        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isPartnerChatting = false
                snapshot.children.forEach { memberSnapshot ->
                    val memberName = memberSnapshot.child("userEmail").getValue(String::class.java)
                    if (memberName == otherEmail) {
                        val memberChatting = memberSnapshot.child("isChatting").getValue(Boolean::class.java) ?: false
                        isPartnerChatting = memberChatting
                        return@forEach
                    }
                }
                val message = Message(
                    myEmail,
                    otherEmail,
                    messageBody.value ?: "",
                    isPartnerChatting,
                    currentTime
                )
                FirebaseData.getIdToken { token ->
                    viewModelScope.launch {
                        chatRepository.sendMessage(token, message, chatRoomKey.value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
            }
        })
    }

    fun getRoomKey(chatRoom: ChatRoom) {
        viewModelScope.launch {
            val chatRoomKey =
                chatRepository.getRoomKey(chatRoom.member.map { it.userEmail }) ?: return@launch
            _chatRoomKey.value = chatRoomKey
        }
    }

    fun listenForChat(roomKey: String) {
        viewModelScope.launch {
            val messageRef = database.getReference("chatRooms/$roomKey/messages")

            messageRef.addValueEventListener(object : ValueEventListener {
                private var initialDataLoaded = false

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!initialDataLoaded) {
                        initialDataLoaded = true
                        return
                    }

                    val messageData = snapshot.children.last()
                    val isMessageReading =
                        messageData.child("reading").getValue(Boolean::class.java) ?: false
                    val message = messageData.getValue(Message::class.java) ?: return
                    message.reading = isMessageReading
                    if (message.sender.isEmpty() || message.receiver.isEmpty()) return
                    _newMessage.value = Event(message)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "DataSnapshotCancelled: $error")
                }
            })
        }
    }

    fun updateIsReadingForMessages() {
        val myEmail = getMyEmail()
        val messagesRef = FirebaseDatabase.getInstance().getReference("chatRooms/${chatRoomKey.value}/messages")
        val memberRef = FirebaseDatabase.getInstance().getReference("chatRooms/${chatRoomKey.value}/member")
        memberRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isPartnerChatting = false
                snapshot.children.forEach { memberSnapshot ->
                    val memberChatting = memberSnapshot.child("isChatting").getValue(Boolean::class.java) ?: false
                    val memberEmail = memberSnapshot.child("userEmail").getValue(String::class.java)
                    if(memberEmail != myEmail) {
                        isPartnerChatting = memberChatting
                        return@forEach
                    }
                }

                messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { messageSnapshot ->
                            val message = messageSnapshot.getValue(Message::class.java)
                            if(message?.reading == false) {
                                messageSnapshot.ref.child("reading").setValue(isPartnerChatting)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "MessageDataSnapshotCancelled: $error")
                    }

                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "MemberDataSnapshotCancelled: $error")
            }

        })
    }
    companion object {
        private const val TAG = "ChatViewModel"
    }
}