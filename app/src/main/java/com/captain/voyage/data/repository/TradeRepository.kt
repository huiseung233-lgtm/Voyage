package com.captain.voyage.data.repository

import com.captain.voyage.data.local.TradeDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.InventoryItemDto
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.ItemType
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.ShipStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradeRepository @Inject constructor(
    private val tradeDao: TradeDao,
    private val voyageDao: VoyageDao
) {

    suspend fun initializeTradeData() {
        val items = tradeDao.getAllItems().first()
        if (items.isEmpty()) {
            val newItems = listOf(
                Item(id = 1, name = "쌀", basePrice = 10, description = "주식. 식량으로 전환 가능하다.", type = ItemType.FOOD, effectValue = 30),
                Item(id = 2, name = "목재", basePrice = 20, description = "건축 자재. 희망의 섬 특산물.", type = ItemType.TRADE_GOOD),
                Item(id = 3, name = "향신료", basePrice = 100, description = "귀한 향신료. 거친 파도 항구 특산물.", type = ItemType.TRADE_GOOD),
                Item(id = 4, name = "철광석", basePrice = 50, description = "단단한 금속. 신대륙에서 발견된다.", type = ItemType.TRADE_GOOD)
            )
            tradeDao.insertItems(newItems)

            val markets = listOf(
                Market(portId = 1, itemId = 1, buyPrice = 8, sellPrice = 6, stock = 100),
                Market(portId = 1, itemId = 2, buyPrice = 25, sellPrice = 20, stock = 50),
                Market(portId = 2, itemId = 1, buyPrice = 15, sellPrice = 12, stock = 50),
                Market(portId = 2, itemId = 2, buyPrice = 15, sellPrice = 12, stock = 100),
                Market(portId = 3, itemId = 3, buyPrice = 80, sellPrice = 70, stock = 20),
                Market(portId = 3, itemId = 1, buyPrice = 20, sellPrice = 18, stock = 50),
                Market(portId = 4, itemId = 4, buyPrice = 40, sellPrice = 30, stock = 200),
                Market(portId = 4, itemId = 2, buyPrice = 30, sellPrice = 25, stock = 100)
            )
            tradeDao.insertMarkets(markets)
        }
    }

    fun getInventoryFlow(): Flow<List<Pair<Item, Int>>> {
        return tradeDao.getInventoryWithItems(1).map { dtoList: List<InventoryItemDto> ->
            dtoList.map { dto ->
                Pair(dto.item, dto.quantity)
            }
        }
    }

    fun getMarketDataFlow(portId: Long): Flow<List<Triple<Market, Item, Int>>> {
        val marketFlow = tradeDao.getMarketList(portId)
        val itemsFlow = tradeDao.getAllItems()
        val inventoryFlow = tradeDao.getInventory(1)

        return combine(marketFlow, itemsFlow, inventoryFlow) { markets, items, inventory ->
            markets.mapNotNull { market ->
                val item = items.find { it.id == market.itemId } ?: return@mapNotNull null
                val myQty = inventory.find { it.itemId == market.itemId }?.quantity ?: 0
                Triple(market, item, myQty)
            }
        }
    }

    suspend fun buyItem(itemId: Long, price: Int, quantity: Int): Int {
        val userStatus = voyageDao.getUserStatus().first() ?: return -1
        val totalCost = price * quantity

        if (userStatus.gold < totalCost) return -1

        val newUserStatus = userStatus.copy(gold = userStatus.gold - totalCost)
        voyageDao.insertUserStatus(newUserStatus)

        return tradeDao.safeAddInventory(1, itemId, quantity)
    }

    suspend fun sellItem(itemId: Long, price: Int, quantity: Int): Boolean {
        val result = tradeDao.safeConsumeInventory(1, itemId, quantity)
        if (result == -1) return false

        val userStatus = voyageDao.getUserStatus().first() ?: return false
        val newUserStatus = userStatus.copy(gold = userStatus.gold + (price * quantity))
        voyageDao.insertUserStatus(newUserStatus)

        return true
    }

    suspend fun loadSupplyToShip(itemId: Long): String {
        val currentShip = voyageDao.getShip().first() ?: return "선박 정보 없음"
        val itemInfo = tradeDao.getAllItems().first().find { it.id == itemId } ?: return "아이템 정보 없음"

        if (itemInfo.type != ItemType.FOOD) return "식량이 아닙니다."

        val newQty = tradeDao.safeConsumeInventory(1, itemId, 1)
        if (newQty == -1) return "수량 부족"

        val addedSupply = itemInfo.effectValue.toDouble()
        val newSupplies = (currentShip.supplies + addedSupply).coerceAtMost(currentShip.maxSupplies)

        val updatedShip = currentShip.copy(supplies = newSupplies)
        voyageDao.updateShip(updatedShip) // [Fix] insertShip -> updateShip

        return "보급 완료! +${itemInfo.effectValue} (남은 재고: ${newQty})"
    }
}
