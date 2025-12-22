package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BuildingType(val displayName: String, val description: String) {
    HEADQUARTERS("본부", "정착지의 중심입니다. 다른 건물의 최대 레벨을 결정합니다."),
    WAREHOUSE("창고", "물자를 보관합니다. 정착지 무역 한도를 늘립니다."),
    SHIPYARD("조선소", "배를 수리하고 개조합니다. 수리비를 절감합니다."),
    MARKETPLACE("시장", "상거래를 활성화하여 세금 수입을 늘립니다.")
}

@Entity(
    tableName = "buildings",
    foreignKeys = [
        ForeignKey(
            entity = Settlement::class,
            parentColumns = ["id"],
            childColumns = ["settlementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["settlementId", "type"], unique = true)]
)
data class Building(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val settlementId: Long,
    val type: BuildingType,
    val level: Int = 1
)
