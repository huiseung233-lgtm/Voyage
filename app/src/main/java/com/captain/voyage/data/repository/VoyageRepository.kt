package com.captain.voyage.data.repository

import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.GoalDao
import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.TradeDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.ItemType
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipInventory
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.sqrt

// [New] 브리핑 데이터 클래스
data class DailyBriefing(
    val yesterdayScore: Int,
    val targetScore: Int,
    val isSuccess: Boolean,
    val moveDistance: Double
)

class VoyageRepository(
    private val voyageDao: VoyageDao,
    private val scoreRecordDao: ScoreRecordDao,
    private val dailyLogDao: DailyLogDao,
    private val goalDao: GoalDao,
    private val portDao: PortDao,
    private val tradeDao: TradeDao,
    private val externalScope: CoroutineScope
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
                Port(id = 2, name = "희망의 섬", description = "풍부한 자원이 잠들어 있는 섬", posX = 800.0, posY = 400.0),
                Port(id = 3, name = "거친 파도 항구", description = "숙련된 선원들만 머무는 곳", posX = 300.0, posY = 1200.0)
            )
            portDao.insertPorts(dummyPorts)
        }
        
        // [New] 무역 데이터 초기화
        initializeTradeData()
    }
    
    private suspend fun initializeTradeData() {
        val items = tradeDao.getAllItems().first()
        if (items.isEmpty()) {
            val newItems = listOf(
                Item(id = 1, name = "쌀", basePrice = 10, description = "주식. 어디서나 잘 팔린다."),
                Item(id = 2, name = "목재", basePrice = 20, description = "건축 자재. 희망의 섬 특산물."),
                Item(id = 3, name = "향신료", basePrice = 100, description = "귀한 향신료. 거친 파도 항구 특산물.")
            )
            tradeDao.insertItems(newItems)
            
            val markets = listOf(
                Market(portId = 1, itemId = 1, buyPrice = 8, sellPrice = 6, stock = 100),
                Market(portId = 1, itemId = 2, buyPrice = 25, sellPrice = 20, stock = 50),
                Market(portId = 2, itemId = 1, buyPrice = 15, sellPrice = 12, stock = 50),
                Market(portId = 2, itemId = 2, buyPrice = 15, sellPrice = 12, stock = 100),
                Market(portId = 3, itemId = 3, buyPrice = 80, sellPrice = 70, stock = 20),
                Market(portId = 3, itemId = 1, buyPrice = 20, sellPrice = 18, stock = 50)
            )
            tradeDao.insertMarkets(markets)
        }
    }

    // ----------------------------------------------------------------
    // 6. [New] 무역 시스템 (Trade System)
    // ----------------------------------------------------------------
    
    // UI용 결합 데이터 반환
    fun getMarketDataFlow(portId: Long): Flow<List<Triple<Market, Item, Int>>> {
        val marketFlow = tradeDao.getMarketList(portId)
        val itemsFlow = tradeDao.getAllItems()
        val inventoryFlow = tradeDao.getInventory(1)

        return combine(marketFlow, itemsFlow, inventoryFlow) { markets, items, inventory ->
            markets.mapNotNull { market ->
                val item = items.find { it.id == market.itemId } ?: return@mapNotNull null
                val myQty = inventory.find { it.itemId == market.itemId }?.quantity ?: 0
                Triple(market, item, myQty)
            }
        }
    }

    suspend fun buyItem(itemId: Long, price: Int, quantity: Int): Boolean {
        val userStatus = voyageDao.getUserStatus().first() ?: return false
        val totalCost = price * quantity
        
        if (userStatus.gold < totalCost) return false // 돈 부족

        // 골드 차감
        val newUserStatus = userStatus.copy(gold = userStatus.gold - totalCost)
        voyageDao.insertUserStatus(newUserStatus)

        // 인벤토리 추가
        val currentInv = tradeDao.getInventory(1).first().find { it.itemId == itemId }
        val newQty = (currentInv?.quantity ?: 0) + quantity
        tradeDao.insertInventory(ShipInventory(shipId = 1, itemId = itemId, quantity = newQty))
        
        return true
    }

    suspend fun sellItem(itemId: Long, price: Int, quantity: Int): Boolean {
        val currentInv = tradeDao.getInventory(1).first().find { it.itemId == itemId }
        val currentQty = currentInv?.quantity ?: 0
        
        if (currentQty < quantity) return false // 재고 부족

        // 인벤토리 차감
        val newQty = currentQty - quantity
        if (newQty > 0) {
            tradeDao.updateInventoryQuantity(1, itemId, newQty)
        } else {
            tradeDao.deleteInventoryItem(1, itemId)
        }

        // 골드 증가
        val userStatus = voyageDao.getUserStatus().first() ?: return false
        val newUserStatus = userStatus.copy(gold = userStatus.gold + (price * quantity))
        voyageDao.insertUserStatus(newUserStatus)
        
        return true
    }

    // ----------------------------------------------------------------
    // 7. 항해 엔진 및 브리핑
    // ----------------------------------------------------------------

    suspend fun setDestination(targetX: Float, targetY: Float) {
        val currentShip = voyageDao.getShip().first() ?: return
        val ports = portDao.getAllPorts().first()
        
        val snapRadius = 40.0
        var destX = targetX.toDouble()
        var destY = targetY.toDouble()
        
        for (port in ports) {
            val dx = port.posX - targetX
            val dy = port.posY - targetY
            if (sqrt(dx*dx + dy*dy) <= snapRadius) {
                destX = port.posX
                destY = port.posY
                break
            }
        }

        val updatedShip = currentShip.copy(
            destX = destX,
            destY = destY
        )
        voyageDao.insertShip(updatedShip)
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
        val moveDistance = if (isSuccess) 100.0 else 50.0
        
        return DailyBriefing(yesterdayScore, targetScore, isSuccess, moveDistance)
    }

    suspend fun settleDailySailing(isSuccess: Boolean): String {
        val currentShip = voyageDao.getShip().first() ?: return "선박 정보 없음"
        val moveDistance = if (isSuccess) 100.0 else 50.0
        
        if (currentShip.destX == null || currentShip.destY == null) {
            return "목적지 없음 (X:${currentShip.destX}, Y:${currentShip.destY})"
        }

        val dx = currentShip.destX - currentShip.posX
        val dy = currentShip.destY - currentShip.posY
        val totalDistance = sqrt(dx * dx + dy * dy)
        
        if (totalDistance < 1.0) {
             return "이미 목적지에 있습니다. (거리: $totalDistance)"
        }
        
        var newX = currentShip.posX
        var newY = currentShip.posY
        var newDestX = currentShip.destX
        var newDestY = currentShip.destY
        var newStatus = currentShip.status
        var message = ""

        if (totalDistance <= moveDistance) {
            newX = currentShip.destX
            newY = currentShip.destY
            newDestX = null
            newDestY = null
            newStatus = ShipStatus.ANCHORED
            message = "목적지 도착 완료! (이동: ${totalDistance.toInt()}km)"
        } else {
            newX = currentShip.posX + (dx / totalDistance) * moveDistance
            newY = currentShip.posY + (dy / totalDistance) * moveDistance
            message = "항해 중... ${moveDistance.toInt()}km 전진! (남은 거리: ${(totalDistance - moveDistance).toInt()}km)"
        }

        val updatedShip = currentShip.copy(
            posX = newX,
            posY = newY,
            destX = newDestX,
            destY = newDestY,
            status = newStatus
        )
        voyageDao.insertShip(updatedShip)
        return message
    }

    fun startVoyage() {
        if (sailingJob?.isActive == true) return

        sailingJob = externalScope.launch {
            while (isActive) {
                delay(1000L)
                val currentShip = voyageDao.getShip().first() ?: break
                if (currentShip.status != ShipStatus.SAILING) break
                if (currentShip.supplies <= 0) {
                    val driftShip = currentShip.copy(
                        supplies = 0,
                        status = ShipStatus.ANCHORED
                    )
                    voyageDao.insertShip(driftShip)
                    break
                }
                
                val newSupplies = currentShip.supplies - 1
                val updatedShip = currentShip.copy(
                    supplies = newSupplies
                )
                voyageDao.insertShip(updatedShip)
            }
        }
    }

    fun stopVoyage() {
        sailingJob?.cancel()
        sailingJob = null
    }
}
