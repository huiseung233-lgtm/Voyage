package com.captain.voyage

import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.GoalDao
import com.captain.voyage.data.local.PortDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.SettlementDao
import com.captain.voyage.data.local.TradeDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.repository.VoyageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class VoyageRepositoryTest {

    @Mock lateinit var voyageDao: VoyageDao
    @Mock lateinit var scoreRecordDao: ScoreRecordDao
    @Mock lateinit var dailyLogDao: DailyLogDao
    @Mock lateinit var goalDao: GoalDao
    @Mock lateinit var portDao: PortDao
    @Mock lateinit var tradeDao: TradeDao
    @Mock lateinit var settlementDao: SettlementDao

    private lateinit var repository: VoyageRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = VoyageRepository(
            voyageDao, scoreRecordDao, dailyLogDao, goalDao, portDao, tradeDao, settlementDao,
            CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `moveShip_whenFuelIsInsufficient_shouldRemainSailing`() = runBlocking {
        // Given
        val startShip = Ship(
            id = 1,
            name = "TestShip",
            posX = 0.0, posY = 0.0,
            destX = 100.0, destY = 0.0, // 목적지까지 100km
            remainingDistance = 50.0,   // 연료는 50km 뿐
            status = ShipStatus.SAILING,
            supplies = 100.0
        )

        `when`(voyageDao.getShip()).thenReturn(flowOf(startShip))
        `when`(voyageDao.insertShip(any())).thenReturn(1L) // 저장은 성공한다고 가정

        // When
        repository.moveShipTowardDestination()

        // Then
        val captor = org.mockito.ArgumentCaptor.forClass(Ship::class.java)
        org.mockito.Mockito.verify(voyageDao).insertShip(captor.capture())
        
        val savedShip = captor.value
        assertEquals("Ship should remain SAILING if not arrived", ShipStatus.SAILING, savedShip.status)
        assertEquals("Remaining distance should be 0", 0.0, savedShip.remainingDistance, 0.01)
    }
}
