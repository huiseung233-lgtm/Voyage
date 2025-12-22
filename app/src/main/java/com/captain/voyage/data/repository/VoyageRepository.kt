package com.captain.voyage.data.repository

import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.GoalDao
import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class VoyageRepository(
    private val voyageDao: VoyageDao,
    private val scoreRecordDao: ScoreRecordDao,
    private val dailyLogDao: DailyLogDao,
    private val goalDao: GoalDao,
    private val portDao: PortDao,
    private val externalScope: CoroutineScope // 앱 전체 생명주기 스코프
) {

    // 항해 루프 제어용 Job
    private var sailingJob: Job? = null

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

    suspend fun updateRulesOrder(rules: List<Rule>) {
        voyageDao.updateRulesOrder(rules)
    }


    // ----------------------------------------------------------------
    // 2. 선박(Ship) & 유저(User) 상태 업무
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
    // 3. 항해 일지 & 점수판
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

    fun getMonthlyLogs(yearMonth: String): Flow<List<DailyLog>> {
        return dailyLogDao.getLogsByMonth("$yearMonth%")
    }

    // ----------------------------------------------------------------
    // 4. 목표(Goals) 관리
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
    // 5. 항구(Ports) 및 지도 관련 업무
    // ----------------------------------------------------------------
    val allPorts: Flow<List<Port>> = portDao.getAllPorts()

    suspend fun initializeDummyPorts() {
        val currentPorts = portDao.getAllPorts().first()
        if (currentPorts.isEmpty()) {
            val dummyPorts = listOf(
                Port(id = 1, name = "시작의 항구", description = "모든 여행이 시작되는 곳", posX = 100.0, posY = 100.0),
                Port(id = 2, name = "희망의 섬", description = "풍부한 자원이 잠들어 있는 섬", posX = 500.0, posY = 300.0),
                Port(id = 3, name = "거친 파도 항구", description = "숙련된 선원들만 머무는 곳", posX = 200.0, posY = 600.0)
            )
            portDao.insertPorts(dummyPorts)
        }
    }

    // [New] 목적지 설정 (자석 효과 포함)
    suspend fun setDestination(targetX: Float, targetY: Float) {
        val currentShip = voyageDao.getShip().first() ?: return
        val ports = portDao.getAllPorts().first()
        
        // 1. 근처 항구 찾기 (Snapping) - 40px 반경
        val snapRadius = 40.0
        var destX = targetX.toDouble()
        var destY = targetY.toDouble()
        
        for (port in ports) {
            val dx = port.posX - targetX
            val dy = port.posY - targetY
            if (sqrt(dx*dx + dy*dy) <= snapRadius) {
                destX = port.posX
                destY = port.posY
                break // 가장 가까운 하나만 잡음
            }
        }

        // 2. 목적지 업데이트 및 상태 변경 (출항 준비 완료)
        // 주의: 여기서 status를 바로 SAILING으로 바꾸지는 않음. 사용자가 '출항하기'를 눌러야 함.
        // 하지만 이미 항해 중이라면 즉시 경로 변경됨.
        val updatedShip = currentShip.copy(
            destX = destX,
            destY = destY
        )
        voyageDao.insertShip(updatedShip)
    }

    // ----------------------------------------------------------------
    // 6. [New] 항해 엔진 (Voyage Engine) - 중앙 제어
    // ----------------------------------------------------------------

    fun startVoyage() {
        if (sailingJob?.isActive == true) return

        sailingJob = externalScope.launch {
            while (isActive) {
                delay(1000L) // 1초마다 갱신 (테스트용)

                val currentShip = voyageDao.getShip().first() ?: break

                // 상태 체크
                if (currentShip.status != ShipStatus.SAILING) break

                // 식량 체크
                if (currentShip.supplies <= 0) {
                    val driftShip = currentShip.copy(
                        supplies = 0,
                        status = ShipStatus.ANCHORED // 강제 정박
                    )
                    voyageDao.insertShip(driftShip)
                    break
                }
                
                // 이동 로직 (Vector)
                var newX = currentShip.posX
                var newY = currentShip.posY
                var newDestX = currentShip.destX
                var newDestY = currentShip.destY
                var newStatus = currentShip.status
                
                if (currentShip.destX != null && currentShip.destY != null) {
                    val dx = currentShip.destX - currentShip.posX
                    val dy = currentShip.destY - currentShip.posY
                    val distance = sqrt(dx * dx + dy * dy)
                    val speed = 5.0 // 이동 속도
                    
                    if (distance <= speed) {
                        // 도착!
                        newX = currentShip.destX
                        newY = currentShip.destY
                        newDestX = null
                        newDestY = null
                        newStatus = ShipStatus.ANCHORED // 도착하면 정박
                    } else {
                        // 이동 중
                        newX = currentShip.posX + (dx / distance) * speed
                        newY = currentShip.posY + (dy / distance) * speed
                    }
                } else {
                    // 목적지가 없으면? 일단 멈춤 (표류하지 않고 대기)
                    // 만약 '자유 항해' 모드라면 그냥 직진하게 할 수도 있음.
                    // 현재는 목적지가 없으면 이동하지 않음.
                }

                // 업데이트 및 저장
                val newSupplies = currentShip.supplies - 1
                val updatedShip = currentShip.copy(
                    posX = newX,
                    posY = newY,
                    destX = newDestX,
                    destY = newDestY,
                    status = newStatus,
                    supplies = newSupplies
                )
                voyageDao.insertShip(updatedShip)
                
                // 도착해서 정박했으면 루프 종료
                if (newStatus == ShipStatus.ANCHORED) break
            }
        }
    }

    fun stopVoyage() {
        sailingJob?.cancel()
        sailingJob = null
    }
}
