package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val date: String, // 날짜 자체가 ID (YYYY-MM-DD)
    val totalScore: Int = 0       // 그날의 최종 합산 점수
)