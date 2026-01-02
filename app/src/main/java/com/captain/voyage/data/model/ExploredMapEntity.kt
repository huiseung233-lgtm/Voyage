package com.captain.voyage.data.model

import androidx.room.Entity

@Entity(tableName = "explored_map", primaryKeys = ["chunkX", "chunkY"])
data class ExploredMapEntity(
    val chunkX: Int,
    val chunkY: Int
)
