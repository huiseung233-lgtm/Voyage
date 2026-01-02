package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.captain.voyage.data.model.ExploredMapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExploredMapDao {
    @Query("SELECT * FROM explored_map")
    fun getAllExploredChunks(): Flow<List<ExploredMapEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChunks(chunks: List<ExploredMapEntity>)

    @Query("DELETE FROM explored_map")
    suspend fun clearExploredMap()
}
