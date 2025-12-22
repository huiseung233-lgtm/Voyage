package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
// 배의 현재 상태 (Enum)
enum class ShipStatus {
    SAILING,    // 항해 중 (정상)
    ANCHORED,   // 정박 중 (휴식)
    DOOMED      // 난파/패널티 상태
}
@Entity(tableName = "ship") // <--- ★ 여기 추가!
data class Ship(
    @PrimaryKey // <--- ★ 여기 추가!
    val id: Int = 1,                 // 배는 보통 1개만 관리하므로 ID는 1로 고정
    val name: String,                // 배 이름 (예: 황금 호)
    val level: Int = 1,              // 배 레벨
    val exp: Long = 0,               // 현재 경험치
    val skinId: String = "default_wood", // 스킨 리소스 이름 (Semantic Naming)
    val status: ShipStatus = ShipStatus.ANCHORED // 기본 상태
)