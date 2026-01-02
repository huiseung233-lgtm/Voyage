package com.captain.voyage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.captain.voyage.data.local.*
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.repository.VoyageRepository
import com.captain.voyage.utils.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class VoyageRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var voyageDao: VoyageDao
    private lateinit var scoreRecordDao: ScoreRecordDao
    private lateinit var dailyLogDao: DailyLogDao
    private lateinit var goalDao: GoalDao
    private lateinit var portDao: PortDao
    private lateinit var tradeDao: TradeDao
    private lateinit var settlementDao: SettlementDao

    private lateinit var repository: VoyageRepository

    @Before
    fun setup() {
        voyageDao = mock()
        scoreRecordDao = mock()
        dailyLogDao = mock()
        goalDao = mock()
        portDao = mock()
        tradeDao = mock()
        settlementDao = mock()
        
        repository = VoyageRepository(
            voyageDao,
            CoroutineScope(mainDispatcherRule.testDispatcher)
        )
    }

    @Test
    fun `check_mocks_are_not_null`() {
        assertNotNull(voyageDao)
        assertNotNull(repository)
    }

    @Test
    fun `moveShip_whenFuelIsInsufficient_shouldRemainSailing`() = runTest {
        // Given
        val startShip = Ship(
            id = 1,
            name = "TestShip",
            posX = 0.0, posY = 0.0,
            destX = 100.0, destY = 0.0,
            remainingDistance = 50.0,
            status = ShipStatus.SAILING,
            supplies = 100.0
        )

        whenever(voyageDao.getShip()).thenReturn(flowOf(startShip))
        whenever(voyageDao.insertShip(any())).thenReturn(1L)

        // When
        repository.moveShipTowardDestination()

        // Then
        val captor = argumentCaptor<Ship>()
        verify(voyageDao, atLeastOnce()).insertShip(captor.capture())
        
        val savedShip = captor.firstValue
        assertEquals("Ship should remain SAILING", ShipStatus.SAILING, savedShip.status)
        assertEquals("Distance should be 0", 0.0, savedShip.remainingDistance, 0.01)
    }
}
