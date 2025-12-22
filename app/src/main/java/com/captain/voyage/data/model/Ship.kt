package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
// 배의 현재 상태 (Enum)
enum class ShipStatus {
    SAILING,    // 항해 중 (정상)
    ANCHORED,   // 정박 중 (휴식)
    DOOMED      // 난파/패널티 상태
}
@Entity(tableName = "ships")
data class Ship(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val level: Int = 1,
    val exp: Int = 0,
    val skinId: String = "0",
    val status: ShipStatus = ShipStatus.ANCHORED,

    // --- 항해 데이터 ---
    val posX: Double = 0.0,      // 현재 X 좌표
    val posY: Double = 0.0,      // 현재 Y 좌표
    val destX: Double? = null,   // 목적지 X 좌표 (null이면 정박 중)
    val destY: Double? = null,   // 목적지 Y 좌표
    val supplies: Int = 100,     // 현재 식량/물 (0~100)
    val maxSupplies: Int = 100   // 최대 적재량
)