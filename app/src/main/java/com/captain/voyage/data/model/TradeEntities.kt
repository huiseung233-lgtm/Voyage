package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// 1. 교역품 (Item)
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,        // 예: "쌀", "후추", "비단"
    val basePrice: Int,      // 기본 가격
    val description: String,
    val type: ItemType = ItemType.TRADE_GOOD, // 무역품, 소비품 등 구분
    val effectValue: Int = 0 // [New] 사용 시 효과값 (예: 식량 +30)
)

enum class ItemType {
    TRADE_GOOD, // 교역품 (시세 차익용)
    FOOD,       // 식량 (보급품)
    SUPPLY,     // 기타 보급품 (물 등)
    EQUIPMENT   // 장비 (대포 등)
}

// [Optimization] JOIN 쿼리 결과 매핑용 DTO
data class InventoryItemDto(
    @androidx.room.Embedded val item: Item,
    val quantity: Int
)

// 2. 항구별 시장 (Market)
// 어떤 항구에서, 어떤 아이템을, 얼마에 파는가?
@Entity(
    tableName = "markets",
    primaryKeys = ["portId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = Port::class, parentColumns = ["id"], childColumns = ["portId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        androidx.room.Index(value = ["portId"]),
        androidx.room.Index(value = ["itemId"])
    ]
)
data class Market(
    val portId: Long,
    val itemId: Long,
    val buyPrice: Int,   // 선장이 살 때 가격 (항구 -> 선장)
    val sellPrice: Int,  // 선장이 팔 때 가격 (선장 -> 항구)
    val stock: Int       // 항구의 재고량 (매진될 수 있음)
)

// 3. 시세 기록 (PriceHistory) - 나중에 그래프 그리기용
@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val portId: Long,
    val itemId: Long,
    val price: Int,
    val timestamp: Long
)