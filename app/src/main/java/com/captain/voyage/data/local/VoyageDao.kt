package com.captain.voyage.data.local

import androidx.room.*
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.UserStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VoyageDao {

    // --- 1. 규칙(Rule) 관리 ---
    @Query("SELECT * FROM rules ORDER BY orderIndex ASC")
    fun getAllRules(): Flow<List<Rule>> // 데이터가 바뀌면 실시간으로 알려줌 (Flow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: Rule)

    @Delete
    suspend fun deleteRule(rule: Rule)

    @Update
    suspend fun updateRule(rule: Rule)

    // --- 2. 기록(DailyLog) 관리 ---
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    suspend fun getDailyLog(date: String): DailyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLog)

    // --- 3. 선박(Ship) & 유저(UserStatus) 관리 ---
    // 배와 유저 정보는 단 하나만(ID=1) 존재한다고 가정합니다.
    @Query("SELECT * FROM ship WHERE id = 1")
    fun getShip(): Flow<Ship?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShip(ship: Ship)

    @Query("SELECT * FROM user_status WHERE id = 1")
    fun getUserStatus(): Flow<UserStatus?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStatus(userStatus: UserStatus)

    @Update
    suspend fun updateRulesOrder(rules: List<Rule>)
}