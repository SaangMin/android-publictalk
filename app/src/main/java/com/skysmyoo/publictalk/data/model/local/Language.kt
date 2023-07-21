package com.skysmyoo.publictalk.data.model.local

enum class Language(val code: String) {
    KOREA("ko"),
    ENGLISH("en"),
    CHINESE("zh-CN"),
    JAPANESE("ja");

    fun toPosition(): Int {
        return when (this) {
            KOREA -> 0
            ENGLISH -> 1
            CHINESE -> 2
            else -> 3
        }
    }

    companion object {
        fun fromPosition(position: Int): Language {
            return when (position) {
                0 -> KOREA
                1 -> ENGLISH
                2 -> CHINESE
                else -> JAPANESE
            }
        }

        fun fromCode(code: String): Language {
            return when (code) {
                "ko" -> KOREA
                "en" -> ENGLISH
                "zh-CN" -> CHINESE
                "ja" -> JAPANESE
                else -> ENGLISH
            }
        }
    }
}