package com.captain.voyage.data.repository

import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.local.SettlementDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.Building
import com.captain.voyage.data.model.BuildingType
import com.captain.voyage.data.model.Settlement
import com.captain.voyage.utils.GameConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettlementRepository @Inject constructor(
    private val settlementDao: SettlementDao,
    private val portDao: PortDao,
    private val voyageDao: VoyageDao
) {

    fun getSettlementState(portId: Long): Flow<Pair<Settlement?, List<Building>>> {
        val settlementFlow = settlementDao.getSettlementByPortId(portId)
        return settlementFlow.combine(flowOf(Unit)) { settlement, _ ->
            val buildings = if (settlement != null) {
                settlementDao.getBuildingsFlow(settlement.id).first()
            } else {
                emptyList()
            }
            Pair(settlement, buildings)
        }
    }
    
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
}
