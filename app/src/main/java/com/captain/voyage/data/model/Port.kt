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
    val isDiscovered: Boolean = false // 발견 여부 (선형 진행 유도용)
)
