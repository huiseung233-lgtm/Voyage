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
        
        // --- Stepwise Simulation Start ---
        val stepSize = 10.0 // 10km 단위로 이동 체크
        var remainingStepDist = actualMoveDist
        var currX = currentShip.posX
        var currY = currentShip.posY
        var isStuck = false
        
        while (remainingStepDist > 0 && !isStuck) {
            val move = if (remainingStepDist > stepSize) stepSize else remainingStepDist
            
            // 현재 위치에서 목적지까지의 벡터 재계산 (우회하면서 방향이 바뀔 수 있으므로)
            val dxStep = currentShip.destX!! - currX
            val dyStep = currentShip.destY!! - currY
            val distStep = sqrt(dxStep * dxStep + dyStep * dyStep)
            
            if (distStep < 0.1) break // 도착 간주
            
            val dxNorm = dxStep / distStep
            val dyNorm = dyStep / distStep
            
            val tryX = currX + dxNorm * move
            val tryY = currY + dyNorm * move
            
            val collidingLand = WorldData.getCollidingLand(tryX, tryY)
            
            if (collidingLand == null) {
                // 1. 이동 성공
                currX = tryX
                currY = tryY
            } else {
                // 2. 충돌! 물리 기반 슬라이딩 (Vector Projection)
                // 육지 중심에서 배를 향하는 법선 벡터(Normal Vector) 계산
                val normalX = currX - collidingLand.centerX
                val normalY = currY - collidingLand.centerY
                val normalLen = sqrt(normalX * normalX + normalY * normalY)
                
                if (normalLen > 0) {
                    val nx = normalX / normalLen
                    val ny = normalY / normalLen
                    
                    // 이동 벡터(Move)
                    val vx = dxNorm * move
                    val vy = dyNorm * move
                    
                    // Move 벡터를 Normal에 투영 (Dot Product) -> 벽을 미는 힘
                    val dot = vx * nx + vy * ny
                    
                    // Move에서 벽을 미는 힘을 제거 -> 벽을 타고 흐르는 힘(Slide)
                    // Slide = V - (V dot N) * N
                    val slideX = vx - dot * nx
                    val slideY = vy - dot * ny
                    
                    // [Fix] Push out slightly along normal to avoid sticking to the surface
                    val pushOut = 1.0 // 1 unit push
                    val slideTryX = currX + slideX + (nx * pushOut)
                    val slideTryY = currY + slideY + (ny * pushOut)
                    
                    // 슬라이딩 위치가 안전한지 재확인 (2차 충돌 방지)
                    if (!WorldData.isLand(slideTryX, slideTryY)) {
                        currX = slideTryX
                        currY = slideTryY
                    } else {
                        // 그래도 막히면 더 강하게 밀어내보기 (Optional)
                        // 이번엔 그냥 멈춤
                        isStuck = true
                    }
                } else {
                    isStuck = true
                }
            }
            
            if (!isStuck) {
                remainingStepDist -= move
            }
        }
        // --- Stepwise Simulation End ---

        var newX = currX
        var newY = currY
        var newDestX: Double? = currentShip.destX
        var newDestY: Double? = currentShip.destY
        var newStatus = ShipStatus.SAILING
        var message = ""

        // 최종 도착 여부 확인
        val distToDest = sqrt((newX - currentShip.destX!!)*(newX - currentShip.destX!!) + (newY - currentShip.destY!!)*(newY - currentShip.destY!!))
        
        if (distToDest < GameConstants.ARRIVAL_THRESHOLD) {
             newX = currentShip.destX!!
             newY = currentShip.destY!!
             newDestX = null
             newDestY = null
             newStatus = ShipStatus.ANCHORED
             message = "⚓ 목적지 도착 완료! (이동: ${(actualMoveDist - remainingStepDist).toInt()}km)"
        } else {
             if (isStuck) {
                 message = "⛔ 육지에 가로막혀 이동을 멈췄습니다. (이동: ${(actualMoveDist - remainingStepDist).toInt()}km)"
             } else {
                 message = "🌊 순항 중... 오늘의 운항 종료. (남은 거리: ${distToDest.toInt()}km)"
             }
        }
        
        // 잔여 거리 차감 (실제 이동한 만큼만 차감 or 시도한 만큼 차감? 여기선 시도한 만큼)
        // 하지만 Stuck인 경우 억울하니까 실제 이동 거리 기반으로 계산
        val finalMoved = actualMoveDist - remainingStepDist
        val newRemainingDist = (maxDist - finalMoved).coerceAtLeast(0.0)
        
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
