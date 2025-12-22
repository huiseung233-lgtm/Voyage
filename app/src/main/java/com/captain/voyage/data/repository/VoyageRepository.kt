package com.captain.voyage.data.repository

import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.UserStatus
import kotlinx.coroutines.flow.Flow

class VoyageRepository(
    private val voyageDao: VoyageDao,           // 1. 기존: 규칙, 배, 유저
    private val scoreRecordDao: ScoreRecordDao, // 2. 신규: 개별 기록
    private val dailyLogDao: DailyLogDao        // 3. 신규: 일별 점수판
) {

    // ----------------------------------------------------------------
    // 1. 규칙(Rules) 관련 업무
    // ----------------------------------------------------------------
    val allRules: Flow<List<Rule>> = voyageDao.getAllRules()

    suspend fun addRule(rule: Rule) {
        voyageDao.insertRule(rule)
    }

    suspend fun updateRule(rule: Rule) {
        voyageDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: Rule) {
        voyageDao.deleteRule(rule)
    }

    // 드래그 앤 드롭 순서 저장
    suspend fun updateRulesOrder(rules: List<Rule>) {
        voyageDao.updateRulesOrder(rules)
    }


    // ----------------------------------------------------------------
    // 2. 선박(Ship) & 유저(User) 상태 업무 (기존 기능)
    // ----------------------------------------------------------------
    val ship: Flow<Ship?> = voyageDao.getShip()
    val userStatus: Flow<UserStatus?> = voyageDao.getUserStatus()

    suspend fun saveShip(ship: Ship) {
        voyageDao.insertShip(ship)
    }

    suspend fun saveUserStatus(status: UserStatus) {
        voyageDao.insertUserStatus(status)
    }


    // ----------------------------------------------------------------
    // 3. [New] 항해 일지 & 점수판 (Phase 3 기능)
    // ----------------------------------------------------------------

    // (1) 개별 기록(ScoreRecord) 관리
    fun getScoreRecordsByDate(date: String): Flow<List<ScoreRecord>> {
        return scoreRecordDao.getRecordsByDate(date)
    }

    suspend fun insertScoreRecord(record: ScoreRecord) {
        scoreRecordDao.insertRecord(record)
    }

    suspend fun deleteScoreRecord(record: ScoreRecord) {
        scoreRecordDao.deleteRecord(record)
    }

    // (2) 일일 점수판(DailyLog) 관리
    fun getLiveDailyLog(date: String): Flow<DailyLog?> {
        return dailyLogDao.getLiveLogByDate(date)
    }

    fun getAllDailyLogs(): Flow<List<DailyLog>> {
        return dailyLogDao.getAllLogs()
    }

    suspend fun updateDailyLog(log: DailyLog) {
        dailyLogDao.insertOrUpdateLog(log)
    }

    suspend fun getDailyLogDirect(date: String): DailyLog? {
        return dailyLogDao.getLogDirect(date)
    }

    // ... 기존 코드 하단에 추가 ...

    // (3) 월간 데이터 조회 (캘린더용)
    suspend fun getMonthlyLogs(yearMonth: String): List<DailyLog> {
        // "2025-12"를 받으면 "2025-12%"로 변환해서 DAO에 전달
        return dailyLogDao.getLogsByMonth("$yearMonth%")
    }
}