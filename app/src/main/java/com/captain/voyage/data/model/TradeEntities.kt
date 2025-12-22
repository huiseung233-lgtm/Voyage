package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 물품의 종류 (식량, 수리 자재, 교역품 등)
enum class ItemType {
    SUPPLY,     // 식량/물
    MATERIAL,   // 건설 자재
    TRADE_GOOD  // 일반 교역품
}

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: ItemType,
    val basePrice: Int, // 기본 가격
    val weight: Int     // 무게 (적재량 제한용)
)

@Entity(
    tableName = "markets",
    primaryKeys = ["portId", "itemId"] // 복합키: 항구당 물건은 하나씩만 존재
)
data class Market(
    val portId: Long,
    val itemId: Long,
    val buyPrice: Int,  // 유저가 살 때 가격
    val sellPrice: Int, // 유저가 팔 때 가격
    val stock: Int,     // 항구 재고
    val volatility: Float = 0.1f // 변동성 (0.0 ~ 1.0)
)
