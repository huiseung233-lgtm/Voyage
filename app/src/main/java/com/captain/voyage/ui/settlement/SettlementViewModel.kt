package com.captain.voyage.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.Building
import com.captain.voyage.data.model.BuildingType
import com.captain.voyage.data.model.Settlement
import com.captain.voyage.data.repository.VoyageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettlementUiState(
    val settlement: Settlement? = null,
    val buildings: List<Building> = emptyList(),
    val userGold: Long = 0,
    val message: String? = null
)

@HiltViewModel
class SettlementViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // 현재 선택된 항구 ID (UI에서 설정)
    private val _currentPortId = MutableStateFlow<Long?>(null)

    // 정착지 정보 스트림
    private val settlementFlow = _currentPortId.flatMapLatest { portId ->
        if (portId == null) flowOf(null)
        else repository.getSettlement(portId)
    }

    // 건물 목록 스트림
    private val buildingsFlow = settlementFlow.flatMapLatest { settlement ->
        if (settlement == null) flowOf(emptyList())
        else repository.getBuildings(settlement.id)
    }
    
    // 유저 골드 정보
    private val userGoldFlow = repository.userStatus.flatMapLatest { 
        flowOf(it?.gold ?: 0L)
    }

    val uiState: StateFlow<SettlementUiState> = combine(
        settlementFlow,
        buildingsFlow,
        userGoldFlow,
        flowOf(null) // 초기 메시지
    ) { settlement, buildings, gold, _ ->
        SettlementUiState(
            settlement = settlement,
            buildings = buildings,
            userGold = gold
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettlementUiState()
    )
    
    // UI 이벤트 처리를 위한 일회성 메시지 상태 (별도 관리 필요 시)
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage

    fun loadSettlement(portId: Long) {
        _currentPortId.value = portId
    }

    fun foundSettlement(portId: Long, name: String) {
        viewModelScope.launch {
            val result = repository.foundSettlement(portId, name)
            if (result != "Success") {
                _uiMessage.value = result
            } else {
                 _uiMessage.value = "정착지가 건설되었습니다!"
            }
        }
    }

    fun constructBuilding(settlementId: Long, type: BuildingType) {
        viewModelScope.launch {
            val result = repository.constructBuilding(settlementId, type)
            if (result != "Success") {
                _uiMessage.value = result
            } else {
                _uiMessage.value = "건설 완료!"
            }
        }
    }
    
    fun clearMessage() {
        _uiMessage.value = null
    }
}
