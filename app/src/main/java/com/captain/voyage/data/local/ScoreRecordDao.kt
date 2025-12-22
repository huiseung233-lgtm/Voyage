package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.captain.voyage.data.model.ScoreRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreRecordDao {
    // 1. 기록 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ScoreRecord)

    // 2. 기록 삭제
    @Delete
    suspend fun deleteRecord(record: ScoreRecord)

    // 3. 특정 날짜의 기록 가져오기 (항해 일지용, 시간순 정렬)
    @Query("SELECT * FROM score_records WHERE date = :date ORDER BY timestamp ASC")
    fun getRecordsByDate(date: String): Flow<List<ScoreRecord>>

    // 4. 특정 날짜 기록 싹 지우기 (초기화용)
    @Query("DELETE FROM score_records WHERE date = :date")
    suspend fun deleteRecordsByDate(date: String)
}