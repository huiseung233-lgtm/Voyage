package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE type = :type LIMIT 1")
    suspend fun getGoalByType(type: GoalType): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    // --- 점수 집계 쿼리 ---

    // 1. 단순 기간 합계 (Weekly/Monthly용)
    // DailyLog의 totalScore를 합산합니다.
    @Query("SELECT SUM(totalScore) FROM daily_logs WHERE date >= :startDate AND date <= :endDate")
    fun getScoreSumByPeriod(startDate: String, endDate: String): Flow<Int?>

    // 2. 룰 필터링 합계 (Custom용)
    // ScoreRecord 테이블에서 직접 계산합니다.
    // 연결된 룰 ID 목록에 포함되는 기록만 합산합니다.
    @Query("""
        SELECT SUM(score) FROM score_records 
        WHERE date >= :startDate AND date <= :endDate 
        AND ruleId IN (:ruleIds)
    """)
    fun getCustomScoreSum(startDate: String, endDate: String, ruleIds: List<Long>): Flow<Int?>
}
