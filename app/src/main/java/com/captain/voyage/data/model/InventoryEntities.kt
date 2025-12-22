package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

// 선박 인벤토리 (ShipInventory)
// 내 배에 무엇이 얼마나 실려있는가?
@Entity(
    tableName = "ship_inventory",
    primaryKeys = ["shipId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = Ship::class,
            parentColumns = ["id"],
            childColumns = ["shipId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["shipId"]),
        androidx.room.Index(value = ["itemId"])
    ]
)
data class ShipInventory(
    val shipId: Int, // Ship.id와 타입 일치 (Int)
    val itemId: Long, // Item.id와 타입 일치 (Long)
    val quantity: Int
)