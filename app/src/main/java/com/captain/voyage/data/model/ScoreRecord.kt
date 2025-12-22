package com.captain.voyage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_records")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val date: String,           // 날짜 (YYYY-MM-DD) -> 달력에서 빨리 찾기 위해 필수
    val timestamp: Long,        // 언제 수행했는지 (시스템 시간)

    val ruleId: Long?,          // 연결된 규칙 ID (직접 입력인 경우 null 가능)
    val ruleTitle: String,      // 규칙 제목 (나중에 규칙을 지워도 기록엔 남아야 함)

    val score: Int,             // 실제로 얻은 점수 (scoreEarned)
    val note: String = "",      // 간단한 메모 (선택 사항)

    val isCustom: Boolean = false // 직접 입력(커스텀) 여부
)