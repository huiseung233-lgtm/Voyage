package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

// 선박 인벤토리 (ShipInventory)
// 내 배에 무엇이 얼마나 실려있는가?
@Entity(
    tableName = "ship_inventory",
    primaryKeys = ["shipId", "itemId"],
    foreignKeys = [
        // Ship 엔티티의 ID 타입(Int)과 맞춰야 함. Ship.id가 Int라면 여기서도 Int여야 함.
        // 현재 Ship.id는 Int, Item.id는 Long임.
        // 외래키 참조 시 타입 불일치 주의.
    ]
)
data class ShipInventory(
    val shipId: Int, // Ship.id와 타입 일치 (Int)
    val itemId: Long,
    val quantity: Int
)