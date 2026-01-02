package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ports")
data class Port(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val posX: Double,
    val posY: Double,
    val isDiscovered: Boolean = false,
    val canEstablishSettlement: Boolean = false, // 정착지 건설 가능 지역 여부
    val hasShipyard: Boolean = false,
    val hasTavern: Boolean = false,
    val hasMarket: Boolean = false
)
