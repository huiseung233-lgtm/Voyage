package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ship_inventory")
data class ShipInventory(
    @PrimaryKey
    val itemId: Long,
    val count: Int,
    val averageBuyPrice: Int // 수익 계산을 위한 평균 매입가
)

@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val portId: Long,
    val itemId: Long,
    val date: String, // YYYY-MM-DD
    val price: Int
)
