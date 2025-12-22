package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.captain.voyage.data.model.Port
import kotlinx.coroutines.flow.Flow

@Dao
interface PortDao {
    @Query("SELECT * FROM ports")
    fun getAllPorts(): Flow<List<Port>>

    @Query("SELECT * FROM ports WHERE id = :id")
    suspend fun getPortById(id: Long): Port?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPort(port: Port)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPorts(ports: List<Port>)
}
