package com.captain.voyage.data.repository

import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.GoalDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.utils.GameConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class DailyBriefing(
    val yesterdayScore: Int,
    val targetScore: Int,
    val isSuccess: Boolean,
    val moveDistance: Double
)

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val scoreRecordDao: ScoreRecordDao,
    private val dailyLogDao: DailyLogDao
) {

    // ----------------------------------------------------------------
    // 1. 목표(Goals) 관리
    // ----------------------------------------------------------------
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()

    suspend fun saveGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }

    fun getGoalProgress(goal: Goal): Flow<Int?> {
        return if (goal.type == GoalType.CUSTOM) {
            if (goal.linkedRuleIds.isEmpty()) {
                flowOf(0)
            } else {
                goalDao.getCustomScoreSum(goal.startDate, goal.endDate, goal.linkedRuleIds)
            }
        } else {
            goalDao.getScoreSumByPeriod(goal.startDate, goal.endDate)
        }
    }

    // ----------------------------------------------------------------
    // 2. 항해 일지 & 점수판 (Records & Logs)
    // ----------------------------------------------------------------
    fun getScoreRecordsByDate(date: String): Flow<List<ScoreRecord>> {
        return scoreRecordDao.getRecordsByDate(date)
    }

    suspend fun insertScoreRecord(record: ScoreRecord) {
        scoreRecordDao.insertRecord(record)
    }

    suspend fun deleteScoreRecord(record: ScoreRecord) {
        scoreRecordDao.deleteRecord(record)
    }

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

    suspend fun deleteDailyLogByDate(date: String) {
        dailyLogDao.deleteLogByDate(date)
    }

    fun getMonthlyLogs(yearMonth: String): Flow<List<DailyLog>> {
        return dailyLogDao.getLogsByMonth("$yearMonth%")
    }

    // ----------------------------------------------------------------
    // 3. 브리핑 및 성공 여부 판단 (Briefing Logic)
    // ----------------------------------------------------------------
    
    suspend fun hasBriefedToday(): Boolean {
        val today = LocalDate.now().toString()
        val log = dailyLogDao.getLogDirect(today)
        return log != null
    }

    suspend fun checkYesterdaySuccess(): Boolean {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val dailyLog = dailyLogDao.getLogDirect(yesterday)
        val yesterdayScore = dailyLog?.totalScore ?: 0
        val dailyGoal = goalDao.getGoalByType(GoalType.DAILY)
        val targetScore = dailyGoal?.targetScore ?: 100
        return yesterdayScore >= targetScore
    }

    suspend fun getYesterdayBriefing(): DailyBriefing {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val dailyLog = dailyLogDao.getLogDirect(yesterday)
        val yesterdayScore = dailyLog?.totalScore ?: 0
        val dailyGoal = goalDao.getGoalByType(GoalType.DAILY)
        val targetScore = dailyGoal?.targetScore ?: 100
        val isSuccess = yesterdayScore >= targetScore
        val moveDistance = if (isSuccess) GameConstants.DAILY_MOVE_SUCCESS else GameConstants.DAILY_MOVE_FAIL
        
        return DailyBriefing(yesterdayScore, targetScore, isSuccess, moveDistance)
    }
    
    // [Note] confirmDailyBriefing의 '로그 저장' 부분만 분리하고 싶지만, 
    // 지금은 ViewModel에서 호출 순서를 제어하기 어려우므로 VoyageRepository의 confirmDailyBriefing에서 
    // GoalRepository를 호출하거나, ViewModel이 둘 다 호출하게 해야 함.
    // 일단 여기서는 '오늘 로그 생성' 함수만 제공함.
    
    suspend fun createTodayLog() {
        val today = LocalDate.now().toString()
        val todayLog = DailyLog(
            date = today,
            totalScore = 0
        )
        dailyLogDao.insertOrUpdateLog(todayLog)
    }
}
