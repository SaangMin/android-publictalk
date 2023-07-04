package com.skysmyoo.publictalk.data.model.local

import com.skysmyoo.publictalk.data.model.remote.Message

sealed class MessageBox {

    data class SenderMessageBox(val message: Message) : MessageBox() {
        override val id: Message
            get() = message
    }

    data class ReceiverMessageBox(val message: Message) : MessageBox() {
        override val id: Message
            get() = message
    }

    abstract val id: Message
}