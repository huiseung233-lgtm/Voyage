package com.captain.voyage.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.repository.VoyageRepository
import com.captain.voyage.ui.trade.MarketItemUi
import com.captain.voyage.utils.GameConstants // Added
import com.captain.voyage.utils.TimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job // Added
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // Market Flow Job
    private var marketDataJob: Job? = null

    // 1. 실시간 데이터 관찰
    val ship = repository.ship.asLiveData()
    val userStatus = repository.userStatus.asLiveData()
    
    // [New] 지도 표시를 위한 모든 항구 리스트
    val allPorts = repository.allPorts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 현재 항구 감지 (반경 50.0 이내)
    // 항구에 도착했다면 해당 Port 객체를 반환, 아니면 null
    val currentPort = combine(repository.ship, repository.allPorts) { ship, ports ->
        if (ship == null) return@combine null
        ports.find { port ->
            val dx = port.posX - ship.posX
            val dy = port.posY - ship.posY
            sqrt(dx * dx + dy * dy) <= GameConstants.PORT_DETECTION_RADIUS
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentPortId = currentPort.map { it?.id }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAtPort = currentPortId.map { it != null }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 2. UI 알림
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    // 3. 아침 점호 (브리핑) 상태
    private val _briefingData = MutableStateFlow<com.captain.voyage.data.repository.DailyBriefing?>(null)
    val briefingData: StateFlow<com.captain.voyage.data.repository.DailyBriefing?> = _briefingData.asStateFlow()

    private val _showBriefing = MutableStateFlow(false)
    val showBriefing: StateFlow<Boolean> = _showBriefing.asStateFlow()

    // 4. [New] 상점 (무역) 상태
    private val _marketItems = MutableStateFlow<List<MarketItemUi>>(emptyList())
    val marketItems: StateFlow<List<MarketItemUi>> = _marketItems.asStateFlow()

    private val _showMarketDialog = MutableStateFlow(false)
    val showMarketDialog: StateFlow<Boolean> = _showMarketDialog.asStateFlow()

    // 5. [New] 정착지 (Settlement) 상태
    private val _showSettlementDialog = MutableStateFlow(false)
    val showSettlementDialog: StateFlow<Boolean> = _showSettlementDialog.asStateFlow()

    // 6. [New] 인벤토리 (Inventory) 상태
    private val _showInventoryDialog = MutableStateFlow(false)
    val showInventoryDialog: StateFlow<Boolean> = _showInventoryDialog.asStateFlow()

    private val _inventoryItems = MutableStateFlow<List<InventoryItemUi>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItemUi>> = _inventoryItems.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val currentShip = repository.ship.first()
                if (currentShip == null) {
                    val defaultShip = Ship(
                        id = 1,
                        name = "나의 황금선",
                        level = 1,
                        exp = 0,
                        skinId = "0",
                        status = ShipStatus.ANCHORED,
                        posX = 50.0,
                        posY = 50.0
                    )
                    repository.saveShip(defaultShip)
                } else {
                    if (currentShip.status == ShipStatus.SAILING) {
                        repository.startVoyage()
                    }
                }
                // [Cheat] 돈 만땅 주기
                giveMeGold()
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.postValue("데이터 로드 중 오류 발생: ${e.message}")
            }
        }
    }

    // --- 무역 (Trade) ---

    fun openMarket() {
        val portId = currentPortId.value
        if (portId == null) {
            _toastMessage.value = "⛔ 근처에 상점이 있는 항구가 없습니다."
            return
        }

        // 기존 구독 취소 (중복 실행 방지)
        marketDataJob?.cancel()
        
        marketDataJob = viewModelScope.launch {
            // 해당 항구의 마켓 데이터 구독 시작
            repository.getMarketDataFlow(portId).collect { list ->
                val uiItems = list.map { (market, item, qty) ->
                    MarketItemUi(item, market, qty)
                }
                _marketItems.value = uiItems
            }
        }
        _showMarketDialog.value = true
    }

    fun closeMarket() {
        _showMarketDialog.value = false
    }

    fun buyItem(itemUi: MarketItemUi, quantity: Int) {
        viewModelScope.launch {
            val currentQty = repository.buyItem(itemUi.item.id, itemUi.market.buyPrice, quantity)
            if (currentQty != -1) {
                _toastMessage.value = "${itemUi.item.name} 구매 완료! (보유: ${currentQty}개)"
            } else {
                _toastMessage.value = "골드가 부족합니다!"
            }
        }
    }

    fun sellItem(itemUi: MarketItemUi, quantity: Int) {
        viewModelScope.launch {
            val success = repository.sellItem(itemUi.item.id, itemUi.market.sellPrice, quantity)
            if (success) {
                _toastMessage.value = "${itemUi.item.name} 판매 완료!"
            } else {
                _toastMessage.value = "재고가 부족합니다!"
            }
        }
    }

    // [New] 인벤토리 -> 식량 창고 이동
    fun loadSupply(itemId: Long) {
        viewModelScope.launch {
            val resultMsg = repository.loadSupplyToShip(itemId)
            _toastMessage.value = resultMsg
        }
    }

    // --- 항해 및 점호 ---

    fun toggleShipStatus() {
        val currentShip = ship.value ?: return
        if (currentShip.status == ShipStatus.SAILING) {
            dockShip(currentShip)
        } else {
            sailShip(currentShip)
        }
    }

    private fun dockShip(currentShip: Ship) {
        viewModelScope.launch {
            repository.stopVoyage()
            val updatedShip = currentShip.copy(status = ShipStatus.ANCHORED)
            repository.saveShip(updatedShip)
            val effectiveDate = TimeManager.getEffectiveDate().toString()
            _toastMessage.value = "⚓ 정박 완료! (${effectiveDate} 기록 마감)"
        }
    }

    private fun sailShip(currentShip: Ship) {
        if (!TimeManager.canSail()) {
            _toastMessage.value = "⛔ 선박 정비 시간(02:00~07:00)입니다."
        }
        
        // [Changed] 식량 체크 강화
        if (currentShip.supplies < GameConstants.SUPPLY_CONSUMPTION_DAILY) {
             _toastMessage.value = "식량이 부족하여 출항할 수 없습니다! (필요: ${GameConstants.SUPPLY_CONSUMPTION_DAILY})"
             return
        }

        viewModelScope.launch {
            val data = repository.getYesterdayBriefing()
            _briefingData.value = data
            _showBriefing.value = true
        }
    }

    fun confirmBriefingAndSail(hasConfessed: Boolean) {
        val data = _briefingData.value ?: return
        viewModelScope.launch {
            _showBriefing.value = false
            
            val finalSuccess = data.isSuccess && !hasConfessed
            val resultMsg = repository.settleDailySailing(isSuccess = finalSuccess)

            val refreshedShip = repository.ship.first()
            if (refreshedShip != null) {
                val sailingShip = refreshedShip.copy(status = ShipStatus.SAILING)
                repository.saveShip(sailingShip)
                repository.startVoyage()
            }

            val statusMsg = when {
                hasConfessed -> "🚨 규율 위반 처리됨"
                finalSuccess -> "🎉 목표 달성 성공!"
                else -> "☁️ 목표 달성 실패..."
            }
            _toastMessage.value = "☀️ 아침 점호 완료! $statusMsg\n$resultMsg"
        }
    }

    fun dismissBriefing() {
        _showBriefing.value = false
    }

    fun refillSupplies() {
        viewModelScope.launch {
            val currentShip = repository.ship.first() ?: return@launch
            val refilledShip = currentShip.copy(supplies = currentShip.maxSupplies)
            repository.saveShip(refilledShip)
            _toastMessage.postValue("🍞 식량을 가득 채웠습니다! (치트)")
        }
    }

    fun giveMeGold() {
        viewModelScope.launch {
            val status = repository.userStatus.first()
            val newStatus = if (status == null) {
                com.captain.voyage.data.model.UserStatus(
                    id = 1,
                    gold = 1000000L,
                    currentCombo = 0,
                    lastLoginTime = System.currentTimeMillis(),
                    penaltyType = com.captain.voyage.data.model.PenaltyType.NONE
                )
            } else {
                status.copy(gold = 1000000L)
            }
            repository.saveUserStatus(newStatus)
            _toastMessage.postValue("💰 1,000,000 골드를 획득했습니다! (치트)")
        }
    }

    // --- 정착지 (Settlement) ---
    fun openSettlement() {
        _showSettlementDialog.value = true
    }

    fun closeSettlement() {
        _showSettlementDialog.value = false
    }

    // --- 지도 상호작용 ---
    fun setDestination(x: Float, y: Float) {
        viewModelScope.launch {
            repository.setDestination(x, y)
        }
    }

    // --- 인벤토리 ---
    fun openInventory() {
        viewModelScope.launch {
            repository.getInventoryFlow().collect { list ->
                _inventoryItems.value = list.map { (item, qty) ->
                    InventoryItemUi(item, qty)
                }
            }
        }
        _showInventoryDialog.value = true
    }

    fun closeInventory() {
        _showInventoryDialog.value = false
    }
}