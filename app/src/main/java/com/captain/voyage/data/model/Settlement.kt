package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = Port::class,
            parentColumns = ["id"],
            childColumns = ["portId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["portId"], unique = true)]
)
data class Settlement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val portId: Long,
    val name: String,
    val level: Int = 1,
    val foundedAt: Long = System.currentTimeMillis()
)
