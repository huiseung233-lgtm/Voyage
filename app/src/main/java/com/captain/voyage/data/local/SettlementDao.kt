package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.captain.voyage.data.model.Building
import com.captain.voyage.data.model.BuildingType
import com.captain.voyage.data.model.Settlement
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    // --- Settlement ---
    @Query("SELECT * FROM settlements WHERE portId = :portId LIMIT 1")
    fun getSettlementByPortId(portId: Long): Flow<Settlement?>

    @Query("SELECT * FROM settlements WHERE portId = :portId LIMIT 1")
    suspend fun getSettlementByPortIdSync(portId: Long): Settlement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: Settlement): Long

    @Update
    suspend fun updateSettlement(settlement: Settlement)

    // --- Building ---
    @Query("SELECT * FROM buildings WHERE settlementId = :settlementId")
    fun getBuildingsFlow(settlementId: Long): Flow<List<Building>>

    @Query("SELECT * FROM buildings WHERE settlementId = :settlementId AND type = :type LIMIT 1")
    suspend fun getBuilding(settlementId: Long, type: BuildingType): Building?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: Building): Long

    @Update
    suspend fun updateBuilding(building: Building)

    // Transaction: 정착지 생성 시 기본 건물(본부) 자동 건설
    @Transaction
    suspend fun createSettlementWithHeadquarters(settlement: Settlement) {
        val settlementId = insertSettlement(settlement)
        val headquarters = Building(
            settlementId = settlementId,
            type = BuildingType.HEADQUARTERS,
            level = 1
        )
        insertBuilding(headquarters)
    }
}
