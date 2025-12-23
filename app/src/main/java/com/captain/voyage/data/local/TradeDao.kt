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
abstract class TradeDao {

    // --- 아이템 (Item) ---
    @Query("SELECT * FROM items")
    abstract fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertItems(items: List<Item>)

    // --- 시장 (Market) ---
    @Query("SELECT * FROM markets WHERE portId = :portId")
    abstract fun getMarketList(portId: Long): Flow<List<Market>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMarkets(markets: List<Market>)

    // --- 인벤토리 (ShipInventory) ---
    @Query("SELECT * FROM ship_inventory WHERE shipId = :shipId")
    abstract fun getInventory(shipId: Int): Flow<List<ShipInventory>>

    // 내부 사용용 (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertInventory(inventory: ShipInventory)
    
    // 내부 사용용 (UPDATE)
    @Query("UPDATE ship_inventory SET quantity = :quantity WHERE shipId = :shipId AND itemId = :itemId")
    abstract suspend fun updateInventoryQuantity(shipId: Int, itemId: Long, quantity: Int)

    // 내부 사용용 (DELETE)
    @Query("DELETE FROM ship_inventory WHERE shipId = :shipId AND itemId = :itemId")
    abstract suspend fun deleteInventoryItem(shipId: Int, itemId: Long)

    // 내부 사용용 (SELECT ONE)
    @Query("SELECT * FROM ship_inventory WHERE shipId = :shipId AND itemId = :itemId LIMIT 1")
    abstract suspend fun getInventoryItem(shipId: Int, itemId: Long): ShipInventory?

    // [New] DB 원자적 연산: 수량 증가 (성공 시 변경된 행 개수 1 반환)
    @Query("UPDATE ship_inventory SET quantity = quantity + :amount WHERE shipId = :shipId AND itemId = :itemId")
    abstract suspend fun increaseQuantity(shipId: Int, itemId: Long, amount: Int): Int

    // [New] DB 원자적 연산: 수량 감소 (수량이 충분할 때만 차감)
    @Query("UPDATE ship_inventory SET quantity = quantity - :amount WHERE shipId = :shipId AND itemId = :itemId AND quantity >= :amount")
    abstract suspend fun decreaseQuantity(shipId: Int, itemId: Long, amount: Int): Int

    // [Fixed] 안전한 구매 트랜잭션 (Update first, Insert later)
    @Transaction
    open suspend fun safeAddInventory(shipId: Int, itemId: Long, amount: Int): Int {
        // 1. 일단 증가 시도 (DB가 현재 값을 기준으로 더함)
        val rowsUpdated = increaseQuantity(shipId, itemId, amount)
        
        if (rowsUpdated > 0) {
            // 업데이트 성공 -> 현재 값 조회해서 반환
            return getInventoryItem(shipId, itemId)?.quantity ?: amount
        } else {
            // 2. 업데이트 실패 (행이 없음) -> 새로 삽입
            insertInventory(ShipInventory(shipId, itemId, amount))
            return amount
        }
    }

    // [Fixed] 안전한 소모 트랜잭션 (Atomic Decrement)
    @Transaction
    open suspend fun safeConsumeInventory(shipId: Int, itemId: Long, amount: Int): Int {
        // 1. DB 레벨에서 즉시 차감 (수량 부족 시 업데이트 안 됨)
        val rowsUpdated = decreaseQuantity(shipId, itemId, amount)
        
        if (rowsUpdated == 0) return -1 // 수량 부족 또는 아이템 없음

        // 2. 차감 후 수량 확인 (삭제 필요 여부 체크)
        val currentItem = getInventoryItem(shipId, itemId) ?: return 0
        
        if (currentItem.quantity <= 0) {
            deleteInventoryItem(shipId, itemId)
            return 0
        }
        
        return currentItem.quantity
    }
}
