package com.skysmyoo.publictalk.data.model.local

enum class Language(val code: String) {
    KOREA("ko"),
    ENGLISH("en"),
    CHINESE("zh_CN"),
    JAPANESE("ja");

    companion object {
        fun fromPosition(position: Int): Language {
            return when (position) {
                0 -> KOREA
                1 -> ENGLISH
                2 -> CHINESE
                else -> JAPANESE
            }
        }
    }
}