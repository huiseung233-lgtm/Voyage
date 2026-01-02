package com.captain.voyage.data.repository

import com.captain.voyage.data.initial.WorldData
import com.captain.voyage.data.local.ExploredMapDao
import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.local.TradeDao
import com.captain.voyage.data.model.ExploredMapEntity
import com.captain.voyage.data.model.Port
import com.captain.voyage.utils.GameConstants // Added import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class WorldRepository @Inject constructor(
    private val portDao: PortDao,
    private val tradeDao: TradeDao,
    private val exploredMapDao: ExploredMapDao
) {

    val allPorts: Flow<List<Port>> = portDao.getAllPorts()

    // [New] 안개 데이터 (청크 좌표 셋)
    val exploredChunks: Flow<Set<Pair<Int, Int>>> = exploredMapDao.getAllExploredChunks()
        .map { list -> list.map { it.chunkX to it.chunkY }.toSet() }

    fun isLand(x: Double, y: Double): Boolean {
        return WorldData.isLand(x, y)
    }

    // [New] 안개 해제 로직
    suspend fun revealArea(x: Double, y: Double, radius: Double) {
        val worldChunkSize = 100.0
        val minChunkX = ((x - radius) / worldChunkSize).toInt()
        val maxChunkX = ((x + radius) / worldChunkSize).toInt()
        val minChunkY = ((y - radius) / worldChunkSize).toInt()
        val maxChunkY = ((y + radius) / worldChunkSize).toInt()

        val newChunks = mutableListOf<ExploredMapEntity>()
        for (cx in minChunkX..maxChunkX) {
            for (cy in minChunkY..maxChunkY) {
                // 반경 체크 (정확한 원형 해제를 위해)
                val chunkCenterX = cx * worldChunkSize + worldChunkSize / 2
                val chunkCenterY = cy * worldChunkSize + worldChunkSize / 2
                val dx = chunkCenterX - x
                val dy = chunkCenterY - y
                if (sqrt(dx * dx + dy * dy) <= radius + (worldChunkSize / 2)) {
                    newChunks.add(ExploredMapEntity(cx, cy))
                }
            }
        }
        
        if (newChunks.isNotEmpty()) {
            exploredMapDao.insertChunks(newChunks)
        }
    }

    suspend fun revealAllMap() {
        val worldChunkSize = 100.0
        val minIdx = (GameConstants.WORLD_MIN / worldChunkSize).toInt()
        val maxIdx = (GameConstants.WORLD_MAX / worldChunkSize).toInt()
        
        val allChunks = mutableListOf<ExploredMapEntity>()
        for (cx in minIdx..maxIdx) {
            for (cy in minIdx..maxIdx) {
                allChunks.add(ExploredMapEntity(cx, cy))
            }
        }
        exploredMapDao.insertChunks(allChunks)
    }

    suspend fun initializeWorld() {
        val currentPorts = portDao.getAllPorts().first()
        if (currentPorts.isEmpty()) {
            // 1. Ports
            portDao.insertPorts(WorldData.ports)
            
            // 2. Items
            val currentItems = tradeDao.getAllItems().first()
            if (currentItems.isEmpty()) {
                tradeDao.insertItems(WorldData.items)
            }
            
            // 3. Markets
            tradeDao.insertMarkets(WorldData.getInitialMarkets())
        }
    }

    // [New] World Event Logic Placeholder
    // x, y 좌표에서 발생하는 이벤트를 반환 (지금은 예시용 String 반환, 추후 Sealed Class로 변경)
    fun getEventAt(x: Double, y: Double): String? {
        // Example: 특정 좌표 근처에 가면 폭풍우 발생
        if (x > 1000 && y > 1000) return "Storm"
        return null
    }

    // Distance Calculation Helper
    fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}
