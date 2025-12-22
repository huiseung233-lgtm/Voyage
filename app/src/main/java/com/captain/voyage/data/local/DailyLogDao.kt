package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.captain.voyage.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    // 1. 일일 점수 저장/갱신
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: DailyLog)

    // 2. 특정 날짜의 로그 가져오기 (단건)
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    suspend fun getLogByDate(date: String): DailyLog?

    // 3. 모든 날짜 로그 가져오기 (전체 달력용)
    @Query("SELECT * FROM daily_logs")
    fun getAllLogs(): Flow<List<DailyLog>>

    // 4. 오늘 점수 실시간 관찰 (홈 화면용)
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    fun getLiveLogByDate(date: String): Flow<DailyLog?>

    // 5 ★ [New] LiveData가 아니라, 즉시 값을 가져오는 함수 (suspend)
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogDirect(date: String): DailyLog?

    // 6. ★ [New] 특정 월(Month)의 모든 로그 가져오기
    // 사용법: getLogsByMonth("2025-12%") -> 12월 데이터 전부 조회
    @Query("SELECT * FROM daily_logs WHERE date LIKE :monthPattern")
    suspend fun getLogsByMonth(monthPattern: String): List<DailyLog>
}