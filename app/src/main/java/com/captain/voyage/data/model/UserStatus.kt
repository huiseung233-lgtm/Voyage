package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 패널티 종류 (Enum)
enum class PenaltyType {
    NONE,       // 정상
    FATIGUE,    // 피로 (늦게 잠)
    LAZINESS,   // 나태 (지각)
    DOOM        // 최악 (둘 다)
}

@Entity(tableName = "user_status") // <--- ★ 여기 추가!
data class UserStatus(
    @PrimaryKey
    val id: Int = 1,
    val gold: Long = 0,               // 보유 골드
    val currentCombo: Int = 0,        // 연속 성공 일수
    val lastLoginTime: Long = 0,
    val penaltyType: PenaltyType = PenaltyType.NONE,
    val lastSailedDate: String? = null // 마지막 출항(정산) 날짜 (yyyy-MM-dd)
)