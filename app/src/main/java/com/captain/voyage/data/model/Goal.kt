package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalType {
    DAILY, // Added
    WEEKLY,
    MONTHLY,
    CUSTOM
}

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String = "", // Added: 목표 제목 (Custom용)

    val type: GoalType,
    val targetScore: Int,
    
    // 기간 (YYYY-MM-DD)
    val startDate: String,
    val endDate: String,

    // 연결된 규칙 ID 목록 (Custom 목표용)
    val linkedRuleIds: List<Long> = emptyList()
)
