package com.captain.voyage.data.repository

import com.captain.voyage.data.initial.WorldData
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
        
        val updatedShip = currentShip.copy(
            destX = targetX.toDouble(),
            destY = targetY.toDouble()
        )
        saveShip(updatedShip)
    }

    suspend fun confirmDailyBriefing(isSuccess: Boolean): String {
        val currentShip = voyageDao.getShip().first() ?: return "선박 정보 없음"
        
        // 1. 하루치 유지비(식량) 소모
        val dailyCost = GameConstants.SUPPLY_CONSUMPTION_DAILY
        if (currentShip.supplies < dailyCost) {
            val driftShip = currentShip.copy(status = ShipStatus.ANCHORED)
            saveShip(driftShip)
            return "⚠️ 식량이 부족하여 배가 표류했습니다! (충전 실패)"
        }
        val suppliesAfterCost = currentShip.supplies - dailyCost

        // 2. 이동 거리(연료) 충전
        val rechargeAmount = if (isSuccess) GameConstants.DAILY_MOVE_SUCCESS else GameConstants.DAILY_MOVE_FAIL
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
        val dx = currentShip.destX!! - currentShip.posX
        val dy = currentShip.destY!! - currentShip.posY
        val totalDistToDest = sqrt(dx * dx + dy * dy)

        if (totalDistToDest < GameConstants.ARRIVAL_THRESHOLD) {
             return "이미 목적지에 도착해 있습니다."
        }

        // 실제 이동 거리 결정
        val actualMoveDist = if (totalDistToDest <= maxDist) totalDistToDest else maxDist
        
        // 이동 벡터
        val moveRatio = actualMoveDist / totalDistToDest
        val dxMove = dx * moveRatio
        val dyMove = dy * moveRatio

        // 좌표 계산 (충돌 체크 및 슬라이딩)
        var newX = currentShip.posX
        var newY = currentShip.posY
        var newDestX: Double? = currentShip.destX
        var newDestY: Double? = currentShip.destY
        var newStatus = ShipStatus.SAILING
        var message = ""
        
        // 1. Try moving diagonally (Ideal)
        val tryX = currentShip.posX + dxMove
        val tryY = currentShip.posY + dyMove
        
        if (!WorldData.isLand(tryX, tryY)) {
            newX = tryX
            newY = tryY
        } else {
            // Collision detected! Try Sliding.
            // 2. Try moving X only
            val tryXOnly = currentShip.posX + dxMove
            val canMoveX = !WorldData.isLand(tryXOnly, currentShip.posY)
            
            // 3. Try moving Y only
            val tryYOnly = currentShip.posY + dyMove
            val canMoveY = !WorldData.isLand(currentShip.posX, tryYOnly)
            
            if (canMoveX && !canMoveY) {
                newX = tryXOnly
                // Y blocked -> Slide along X
                message = "🚧 육지를 따라 우회합니다. (X축 이동)"
            } else if (!canMoveX && canMoveY) {
                newY = tryYOnly
                // X blocked -> Slide along Y
                message = "🚧 육지를 따라 우회합니다. (Y축 이동)"
            } else if (canMoveX && canMoveY) {
                // Both axes free individually but corner blocked?
                // Or maybe just pick one? Pick the one with larger progress.
                if (kotlin.math.abs(dxMove) > kotlin.math.abs(dyMove)) {
                    newX = tryXOnly
                } else {
                    newY = tryYOnly
                }
                message = "🚧 육지 모서리를 우회합니다."
            } else {
                // Both blocked -> Stuck
                message = "⛔ 전방이 완전히 막혔습니다!"
                newStatus = ShipStatus.ANCHORED // Stop
                newDestX = null
                newDestY = null
            }
        }

        // 목적지 도착 체크
        if (totalDistToDest <= maxDist && !WorldData.isLand(currentShip.destX!!, currentShip.destY!!)) {
             // 원래 목적지에 도달 가능한 경우 (그리고 그곳이 육지가 아닌 경우)
             if (sqrt((newX - currentShip.destX!!)*(newX - currentShip.destX!!) + (newY - currentShip.destY!!)*(newY - currentShip.destY!!)) < GameConstants.ARRIVAL_THRESHOLD) {
                 newX = currentShip.destX!!
                 newY = currentShip.destY!!
                 newDestX = null
                 newDestY = null
                 newStatus = ShipStatus.ANCHORED
                 message = "⚓ 목적지 도착 완료! (이동: ${actualMoveDist.toInt()}km)"
             }
        } else {
             if (message.isEmpty()) {
                 message = "🌊 순항 중... 오늘의 운항 종료. (남은 거리: ${(totalDistToDest - actualMoveDist).toInt()}km)"
             }
        }
        
        // 잔여 거리 차감 (실제 이동량으로 계산하면 좋겠지만, 시도한 만큼 까는게 룰)
        val distMoved = sqrt((newX - currentShip.posX)*(newX - currentShip.posX) + (newY - currentShip.posY)*(newY - currentShip.posY))
        val newRemainingDist = (maxDist - distMoved).coerceAtLeast(0.0)
        
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
            while (isActive) {
                delay(1000L)
                val currentShip = voyageDao.getShip().first() ?: break
                if (currentShip.status != ShipStatus.SAILING) break
            }
        }
    }

    fun stopVoyage() {
        sailingJob?.cancel()
        sailingJob = null
    }

    suspend fun resetShipStatus(): String {
        val currentShip = voyageDao.getShip().first() ?: return "Ship not found"
        val resetShip = currentShip.copy(
            remainingDistance = 0.0,
            supplies = currentShip.maxSupplies
        )
        saveShip(resetShip)
        
        return "🔄 선박 상태가 리셋되었습니다! (식량 충전)"
    }
}
