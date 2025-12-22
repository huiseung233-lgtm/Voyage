package com.captain.voyage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.ShipInventory
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {

    // --- 아이템 (Item) ---
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItems(items: List<Item>)

    // --- 시장 (Market) ---
    // 특정 항구에서 파는 물건 목록 + 가격 정보 조인
    // (간단하게 Market 정보만 가져오고, Item 정보는 별도로 매핑하거나 조인 쿼리 작성)
    @Query("SELECT * FROM markets WHERE portId = :portId")
    fun getMarketList(portId: Long): Flow<List<Market>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarkets(markets: List<Market>)

    // --- 인벤토리 (ShipInventory) ---
    @Query("SELECT * FROM ship_inventory WHERE shipId = :shipId")
    fun getInventory(shipId: Int): Flow<List<ShipInventory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: ShipInventory)
    
    // 재고 업데이트 (수량 변경)
    @Query("UPDATE ship_inventory SET quantity = :quantity WHERE shipId = :shipId AND itemId = :itemId")
    suspend fun updateInventoryQuantity(shipId: Int, itemId: Long, quantity: Int)

    // 아이템 삭제 (수량 0일 때)
    @Query("DELETE FROM ship_inventory WHERE shipId = :shipId AND itemId = :itemId")
    suspend fun deleteInventoryItem(shipId: Int, itemId: Long)
}
