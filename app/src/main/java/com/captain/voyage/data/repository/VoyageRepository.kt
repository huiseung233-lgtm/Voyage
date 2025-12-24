package com.captain.voyage.data.repository

import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.GoalDao
import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.SettlementDao // Added
import com.captain.voyage.data.local.TradeDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.Building // Added
import com.captain.voyage.data.model.BuildingType // Added
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.InventoryItemDto // Added
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.ItemType
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.Settlement // Added
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipInventory
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.model.UserStatus
import com.captain.voyage.utils.GameConstants // Added
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map // Added
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
    private val settlementDao: SettlementDao, // Added
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

    @androidx.room.Transaction
    suspend fun saveShip(ship: Ship) {
        // [Optimization] 불필요한 Update 호출 방지 (Upsert 로직 구현)
        val rowId = voyageDao.insertShip(ship)
        if (rowId == -1L) {
            // 이미 존재하면 업데이트 (외래키 보존)
            voyageDao.updateShip(ship)
        }
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
                Port(id = 3, name = "거친 파도 항구", description = "숙련된 선원들만 머무는 곳", posX = 300.0, posY = 1200.0),
                // [New] 정착지 건설 가능한 신대륙 본토 (맵 하단)
                Port(
                    id = 4, 
                    name = "신대륙 전초기지", 
                    description = "미지의 대륙으로 통하는 관문. 정착지를 건설하기 적합하다.", 
                    posX = 500.0, 
                    posY = 1600.0, // 맵 하단 배치
                    canEstablishSettlement = true
                )
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
                // [New] 쌀은 식량(FOOD), 효과값 30
                Item(id = 1, name = "쌀", basePrice = 10, description = "주식. 식량으로 전환 가능하다.", type = ItemType.FOOD, effectValue = 30),
                Item(id = 2, name = "목재", basePrice = 20, description = "건축 자재. 희망의 섬 특산물.", type = ItemType.TRADE_GOOD),
                Item(id = 3, name = "향신료", basePrice = 100, description = "귀한 향신료. 거친 파도 항구 특산물.", type = ItemType.TRADE_GOOD),
                Item(id = 4, name = "철광석", basePrice = 50, description = "단단한 금속. 신대륙에서 발견된다.", type = ItemType.TRADE_GOOD)
            )
            tradeDao.insertItems(newItems)
            
            val markets = listOf(
                Market(portId = 1, itemId = 1, buyPrice = 8, sellPrice = 6, stock = 100),
                Market(portId = 1, itemId = 2, buyPrice = 25, sellPrice = 20, stock = 50),
                Market(portId = 2, itemId = 1, buyPrice = 15, sellPrice = 12, stock = 50),
                Market(portId = 2, itemId = 2, buyPrice = 15, sellPrice = 12, stock = 100),
                Market(portId = 3, itemId = 3, buyPrice = 80, sellPrice = 70, stock = 20),
                Market(portId = 3, itemId = 1, buyPrice = 20, sellPrice = 18, stock = 50),
                // 신대륙 시장
                Market(portId = 4, itemId = 4, buyPrice = 40, sellPrice = 30, stock = 200),
                Market(portId = 4, itemId = 2, buyPrice = 30, sellPrice = 25, stock = 100) // 목재 수요 높음
            )
            tradeDao.insertMarkets(markets)
        }
    }

    // ----------------------------------------------------------------
    // 6. [New] 무역 시스템 (Trade System)
    // ----------------------------------------------------------------
    
    // 순수 인벤토리 Flow 반환
    fun getInventoryFlow(): Flow<List<Pair<Item, Int>>> {
        // [Optimization] SQL JOIN 사용으로 메모리 연산 제거
        return tradeDao.getInventoryWithItems(1).map { dtoList: List<InventoryItemDto> ->
            dtoList.map { dto ->
                Pair(dto.item, dto.quantity)
            }
        }
    }

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

    // [Changed] Boolean -> Int (구매 후 총 수량 반환, 실패 시 -1)
    suspend fun buyItem(itemId: Long, price: Int, quantity: Int): Int {
        val userStatus = voyageDao.getUserStatus().first() ?: return -1
        val totalCost = price * quantity
        
        if (userStatus.gold < totalCost) return -1 // 돈 부족

        // 골드 차감
        val newUserStatus = userStatus.copy(gold = userStatus.gold - totalCost)
        voyageDao.insertUserStatus(newUserStatus)

        // [Fixed] Dao 트랜잭션 사용 (안전한 구매)
        return tradeDao.safeAddInventory(1, itemId, quantity)
    }

    suspend fun sellItem(itemId: Long, price: Int, quantity: Int): Boolean {
        // [Fixed] Dao 트랜잭션 사용 (안전한 판매)
        val result = tradeDao.safeConsumeInventory(1, itemId, quantity)
        if (result == -1) return false // 재고 부족

        // 골드 증가
        val userStatus = voyageDao.getUserStatus().first() ?: return false
        val newUserStatus = userStatus.copy(gold = userStatus.gold + (price * quantity))
        voyageDao.insertUserStatus(newUserStatus)
        
        return true
    }

    suspend fun loadSupplyToShip(itemId: Long): String {
        val currentShip = voyageDao.getShip().first() ?: return "선박 정보 없음"
        // [Fixed] Flow 대신 suspend 함수로 최신 재고 직접 조회
        // val inventoryItem = tradeDao.getInventoryItem(1, itemId) ?: return "아이템 없음" // Removed
        val itemInfo = tradeDao.getAllItems().first().find { it.id == itemId } ?: return "아이템 정보 없음"

        if (itemInfo.type != ItemType.FOOD) return "식량이 아닙니다."
        
        // [Fixed] Dao 트랜잭션 사용 (안전한 소모)
        val newQty = tradeDao.safeConsumeInventory(1, itemId, 1) // 1개 소모
        if (newQty == -1) return "수량 부족" // 혹은 아이템 없음

        // 식량 충전
        val addedSupply = itemInfo.effectValue.toDouble()
        val newSupplies = (currentShip.supplies + addedSupply).coerceAtMost(currentShip.maxSupplies)
        
        val updatedShip = currentShip.copy(supplies = newSupplies)
        saveShip(updatedShip)
        
        return "보급 완료! +${itemInfo.effectValue} (남은 재고: ${newQty})"
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
        saveShip(updatedShip)
    }

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

    // [Changed] 이름 변경: settleDailySailing -> confirmDailyBriefing
    // 이제 이 함수는 배를 이동시키지 않고, '운항 가능 거리(remainingDistance)'만 충전합니다.
    suspend fun confirmDailyBriefing(isSuccess: Boolean): String {
        val currentShip = voyageDao.getShip().first() ?: return "선박 정보 없음"
        
        // 1. 하루치 유지비(식량) 소모 (정박 중에도 밥은 먹으니까)
        val dailyCost = GameConstants.SUPPLY_CONSUMPTION_DAILY
        if (currentShip.supplies < dailyCost) {
            val driftShip = currentShip.copy(status = ShipStatus.ANCHORED)
            saveShip(driftShip)
            return "⚠️ 식량이 부족하여 배가 표류했습니다! (충전 실패)"
        }
        val suppliesAfterCost = currentShip.supplies - dailyCost

        // 2. 이동 거리(연료) 충전
        val rechargeAmount = if (isSuccess) GameConstants.DAILY_MOVE_SUCCESS else GameConstants.DAILY_MOVE_FAIL
        
        // 기존 잔여량은 초기화(하루 단위 갱신)하거나 더해줄 수 있음. 여기서는 "오늘의 에너지"로 교체(Replace)
        val newRemainingDistance = rechargeAmount

        val updatedShip = currentShip.copy(
            supplies = suppliesAfterCost,
            remainingDistance = newRemainingDistance
        )
        saveShip(updatedShip)
        
        return if (isSuccess) "⚡ 추진력 충전 완료! (+$rechargeAmount km)" else "☁️ 추진력 충전 완료 (+$rechargeAmount km)"
    }

    // [New] 실제 이동 로직 (남은 거리를 소모하여 이동)
    suspend fun moveShipTowardDestination(): String {
        val currentShip = voyageDao.getShip().first() ?: return "선박 정보 없음"

        // 이동 가능 거리 체크
        val maxDist = currentShip.remainingDistance
        if (maxDist <= 0.0) {
            return "오늘 이동 가능한 거리를 모두 소모했습니다. (내일 다시 충전하세요)"
        }

        if (currentShip.destX == null || currentShip.destY == null) {
            return "목적지가 설정되지 않았습니다."
        }

        // 목적지까지 거리 계산
        val dx = currentShip.destX - currentShip.posX
        val dy = currentShip.destY - currentShip.posY
        val totalDistToDest = sqrt(dx * dx + dy * dy)

        if (totalDistToDest < GameConstants.ARRIVAL_THRESHOLD) {
             return "이미 목적지에 도착해 있습니다."
        }

        // 실제 이동 거리 결정 (남은 연료 vs 목적지 거리 중 작은 것)
        val actualMoveDist = if (totalDistToDest <= maxDist) totalDistToDest else maxDist
        
        // 좌표 계산
        var newX: Double
        var newY: Double
        var newDestX: Double? = currentShip.destX
        var newDestY: Double? = currentShip.destY
        var newStatus = ShipStatus.SAILING
        var message = ""

        if (totalDistToDest <= maxDist) {
            // 목적지 도착 가능
            newX = currentShip.destX
            newY = currentShip.destY
            newDestX = null
            newDestY = null
            newStatus = ShipStatus.ANCHORED // 도착했으므로 정박
            message = "⚓ 목적지 도착 완료! (이동: ${actualMoveDist.toInt()}km)"
        } else {
            // 가다가 멈춤 (연료 소진)
            newX = currentShip.posX + (dx / totalDistToDest) * actualMoveDist
            newY = currentShip.posY + (dy / totalDistToDest) * actualMoveDist
            newStatus = ShipStatus.ANCHORED // 연료 다 써서 멈춤
            message = "🌊 순항 중... 오늘의 운항 종료. (이동: ${actualMoveDist.toInt()}km, 남은 거리: ${(totalDistToDest - actualMoveDist).toInt()}km)"
        }
        
        // 잔여 거리 차감
        val newRemainingDist = maxDist - actualMoveDist
        
        // [Optional] 이동 중 식량 추가 소모? (일단 점호 때 깠으니 여기선 패스, 혹은 이동 거리 비례 소모 가능)
        // 여기서는 무료로 이동 (이미 점호 때 냄)

        val updatedShip = currentShip.copy(
            posX = newX,
            posY = newY,
            destX = newDestX,
            destY = newDestY,
            status = newStatus,
            remainingDistance = newRemainingDist
        )
        saveShip(updatedShip)
        
        return message
    }

    fun startVoyage() {
        if (sailingJob?.isActive == true) return

        sailingJob = externalScope.launch {
            // [Changed] 실시간 식량 소모 로직 제거 (일일 정산으로 이동)
            while (isActive) {
                delay(1000L)
                val currentShip = voyageDao.getShip().first() ?: break
                if (currentShip.status != ShipStatus.SAILING) break
                // 상태 모니터링만 유지
            }
        }
    }

    fun stopVoyage() {
        sailingJob?.cancel()
        sailingJob = null
    }

    // ----------------------------------------------------------------
    // 8. [New] 정착지 시스템 (Settlement System)
    // ----------------------------------------------------------------

    fun getSettlementState(portId: Long): Flow<Pair<Settlement?, List<Building>>> {
        val settlementFlow = settlementDao.getSettlementByPortId(portId)
        // Note: settlementFlow emits null initially if not found.
        // We need to handle this. But standard Flow approach is fine.
        // To get buildings, we need settlementId, which we only get after settlementFlow emits.
        // Ideally we use flatMapLatest but for simplicity we can return separate flows or use combine if we had settlementId.
        
        // Since we need to observe both, let's return a combined flow where buildings depend on settlement.
        // However, Room flows are easier to compose if we just expose them or use a transform.
        // Let's optimize: Just return the Settlement flow here, and UI can fetch buildings using settlement.id.
        // Actually, let's try a transform approach for convenience.
        
        return settlementFlow.combine(flowOf(Unit)) { settlement, _ ->
            val buildings = if (settlement != null) {
                settlementDao.getBuildingsFlow(settlement.id).first()
            } else {
                emptyList()
            }
            Pair(settlement, buildings)
        }
    }
    
    // Better approach for UI: Just expose Flows directly or use a specific ViewModel logic.
    // Let's stick to simple DAO exposure for now wrapped in Repository functions.
    
    fun getSettlement(portId: Long): Flow<Settlement?> = settlementDao.getSettlementByPortId(portId)
    
    fun getBuildings(settlementId: Long): Flow<List<Building>> = settlementDao.getBuildingsFlow(settlementId)

    suspend fun foundSettlement(portId: Long, name: String): String {
        val COST = GameConstants.SETTLEMENT_FOUND_COST
        val userStatus = voyageDao.getUserStatus().first() ?: return "유저 정보 없음"
        val port = portDao.getAllPorts().first().find { it.id == portId } ?: return "항구 정보 없음"
        
        if (!port.canEstablishSettlement) {
            return "이 지역에는 정착지를 건설할 수 없습니다."
        }
        
        if (userStatus.gold < COST) return "골드가 부족합니다. (필요: ${COST}G)"
        
        // Check if already exists
        if (settlementDao.getSettlementByPortIdSync(portId) != null) return "이미 정착지가 존재합니다."

        // Deduct Gold
        val newUserStatus = userStatus.copy(gold = userStatus.gold - COST)
        voyageDao.insertUserStatus(newUserStatus)
        
        // Create Settlement
        val settlement = Settlement(portId = portId, name = name)
        settlementDao.createSettlementWithHeadquarters(settlement)
        
        return "Success"
    }

    suspend fun constructBuilding(settlementId: Long, type: BuildingType): String {
        val BASE_COST = GameConstants.BUILDING_BASE_COST
        val userStatus = voyageDao.getUserStatus().first() ?: return "유저 정보 없음"
        
        val existingBuilding = settlementDao.getBuilding(settlementId, type)
        val currentLevel = existingBuilding?.level ?: 0
        val nextLevel = currentLevel + 1
        val cost = nextLevel * BASE_COST
        
        if (userStatus.gold < cost) return "골드가 부족합니다. (필요: ${cost}G)"
        
        // Constraint: Cannot exceed Headquarters level (unless it IS Headquarters)
        if (type != BuildingType.HEADQUARTERS) {
            val hq = settlementDao.getBuilding(settlementId, BuildingType.HEADQUARTERS)
            if (hq != null && nextLevel > hq.level) {
                return "본부 레벨(${hq.level})을 초과할 수 없습니다."
            }
        }

        // Deduct Gold
        val newUserStatus = userStatus.copy(gold = userStatus.gold - cost)
        voyageDao.insertUserStatus(newUserStatus)
        
        // Create or Update
        if (existingBuilding != null) {
            settlementDao.updateBuilding(existingBuilding.copy(level = nextLevel))
        } else {
            settlementDao.insertBuilding(Building(settlementId = settlementId, type = type, level = nextLevel))
        }
        
        return "Success"
    }

    // [Cheat] 오늘 하루 초기화 (다시 점호 받기 위함)
    suspend fun resetDailyStatus(): String {
        val today = LocalDate.now().toString()
        dailyLogDao.deleteLogByDate(today)
        
        val currentShip = voyageDao.getShip().first() ?: return "Ship not found"
        val resetShip = currentShip.copy(
            remainingDistance = 0.0,
            supplies = currentShip.maxSupplies // 식량도 가득!
        )
        saveShip(resetShip)
        
        return "🔄 오늘 하루가 리셋되었습니다! (점호 가능, 식량 충전)"
    }
}
