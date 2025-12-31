package com.captain.voyage.data.repository

import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.model.Port
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class WorldRepository @Inject constructor(
    private val portDao: PortDao
) {

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
