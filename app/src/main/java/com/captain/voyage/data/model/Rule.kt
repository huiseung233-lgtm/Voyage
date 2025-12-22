package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val defaultScore: Int = 10,      // ★ 이걸 쓸 겁니다!
    val penalty: Int = -10,
    val type: RuleType = RuleType.DAILY,
    val orderIndex: Int = 0
    // val score: Int = 10  <-- 🗑️ 이거 삭제하세요!
)

enum class RuleType {
    DAILY, WEEKLY, MONTHLY, SPECIAL
}