package com.captain.voyage.data.repository

import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.model.UserStatus
import com.captain.voyage.utils.GameConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class VoyageRepository(
    private val voyageDao: VoyageDao,
    private val externalScope: CoroutineScope
) {

    // 항해 루프 제어용 Job
    private var sailingJob: Job? = null

    // [New] 정박 프로세스 상태 (Game -> Home 이동 시 유지)
    var isDockingProcess = false

    // [New] 정박 처리 함수 (ViewModel에서 Repository로 이동)
    suspend fun dockShip() {
        stopVoyage()
        val currentShip = voyageDao.getShip().first() ?: return
        val updatedShip = currentShip.copy(status = ShipStatus.ANCHORED)
        saveShip(updatedShip)
        isDockingProcess = false // 프로세스 종료
    }

    // ----------------------------------------------------------------
    // 1. 규칙(Rules) 관련 업무 (Core)
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
    // 2. 선박(Ship) & 유저(User) 상태 업무 (Core)
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
    // 7. 항해 엔진 및 브리핑 (Core Logic)
    // ----------------------------------------------------------------

    suspend fun setDestination(targetX: Float, targetY: Float) {
        val currentShip = voyageDao.getShip().first() ?: return
        
        // [Changed] Snap to port logic moved to ViewModel/WorldRepository interaction.
        // Repository just saves the raw destination.
        
        val updatedShip = currentShip.copy(
            destX = targetX.toDouble(),
            destY = targetY.toDouble()
        )
        saveShip(updatedShip)
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
            // [Fix] 바다 위에서는 정박하지 않고 항해 상태 유지 (SAILING)
            newStatus = ShipStatus.SAILING 
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

    // [Cheat] 오늘 하루 초기화 (다시 점호 받기 위함) -> Reset logic partially needs GoalRepo? 
    // Wait, resetDailyStatus deleted dailyLog. That belongs to GoalRepo.
    // However, it also resets Ship status.
    // Ideally, this cheat function should be in ViewModel calling both Repos.
    // Or keep it here but only reset Ship, and let ViewModel reset Log via GoalRepo.
    // Let's remove the Log deletion part from here to be pure Core.
    
    suspend fun resetShipStatus(): String {
        // dailyLogDao.deleteLogByDate(today) -> REMOVED. ViewModel should call GoalRepo.deleteLog
        
        val currentShip = voyageDao.getShip().first() ?: return "Ship not found"
        val resetShip = currentShip.copy(
            remainingDistance = 0.0,
            supplies = currentShip.maxSupplies // 식량도 가득!
        )
        saveShip(resetShip)
        
        return "🔄 선박 상태가 리셋되었습니다! (식량 충전)"
    }
}