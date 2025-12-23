package com.captain.voyage.data.model

enum class EventType {
    POSITIVE, // 보물 발견, 순풍 등 (이득)
    NEGATIVE, // 폭풍우, 괴수 출현 등 (손해)
    NEUTRAL   // 단순 만남 등
}

data class SailingEvent(
    val title: String,
    val description: String,
    val goldChange: Int = 0,
    val suppliesChange: Int = 0,
    val type: EventType
)
