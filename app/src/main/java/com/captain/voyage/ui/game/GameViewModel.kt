package com.captain.voyage.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.repository.VoyageRepository
import com.captain.voyage.utils.TimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.sqrt

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // 1. 실시간 데이터 관찰
    val ship = repository.ship.asLiveData()
    val userStatus = repository.userStatus.asLiveData()
    
    // 현재 항구에 있는지 여부 판별 (반경 50.0 이내면 도착으로 간주)
    val isAtPort: StateFlow<Boolean> = combine(repository.ship, repository.allPorts) { ship, ports ->
        if (ship == null) return@combine false
        ports.any { port ->
            val dx = port.posX - ship.posX
            val dy = port.posY - ship.posY
            sqrt(dx * dx + dy * dy) <= 50.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 2. UI 알림을 위한 LiveData
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    // ★ [수정됨] skinId를 문자열("0")로 변경하여 타입 에러 해결
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
                    // 앱 재실행 시, 만약 항해 중 상태라면 루프 재시작 (Repository에 위임)
                    if (currentShip.status == ShipStatus.SAILING) {
                        repository.startVoyage()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.postValue("데이터 로드 중 오류 발생: ${e.message}")
            }
        }
    }

    // 3. 정박 / 출항 토글 기능
    fun toggleShipStatus() {
        val currentShip = ship.value

        if (currentShip == null) {
            _toastMessage.value = "⏳ 선박 정보를 불러오는 중입니다..."
            return
        }

        if (currentShip.status == ShipStatus.SAILING) {
            dockShip(currentShip)
        } else {
            sailShip(currentShip)
        }
    }

    private fun dockShip(currentShip: Ship) {
        viewModelScope.launch {
            repository.stopVoyage() // 루프 중단 요청
            val updatedShip = currentShip.copy(status = ShipStatus.ANCHORED)
            repository.saveShip(updatedShip)

            // 안전하게 toString() 유지
            val effectiveDate = TimeManager.getEffectiveDate().toString()
            _toastMessage.value = "⚓ 정박 완료! ($effectiveDate 기록 마감)"
        }
    }

    private fun sailShip(currentShip: Ship) {
        // [테스트를 위해 시간 제한 잠시 해제 가능] - 원칙대로라면 유지
        if (!TimeManager.canSail()) {
            _toastMessage.value = "⛔ 지금은 선박 정비 시간(02:00~07:00)입니다. 출항할 수 없습니다."
            // return // 개발 테스트 중에는 주석 처리하여 언제든 출항 가능하게 함
        }
        
        // 식량 체크
        if (currentShip.supplies <= 0) {
             _toastMessage.value = "식량이 부족하여 출항할 수 없습니다!"
             return
        }

        val isMorning = TimeManager.isMorningSailing()

        viewModelScope.launch {
            // 1. [New] 어제 성과 정산 (일일 정산)
            val isSuccess = repository.checkYesterdaySuccess()
            val resultMsg = repository.settleDailySailing(isSuccess = isSuccess)

            // 2. 오늘의 항해 시작
            // 중요: 정산으로 좌표가 바뀌었으므로, 최신 데이터를 다시 가져와서 상태를 변경해야 함
            val refreshedShip = repository.ship.first()
            if (refreshedShip != null) {
                val sailingShip = refreshedShip.copy(status = ShipStatus.SAILING)
                repository.saveShip(sailingShip)
                repository.startVoyage() // 중앙 엔진 가동!
            }

            if (isMorning) {
                val statusMsg = if (isSuccess) "🎉 목표 달성 성공!" else "☁️ 목표 달성 실패..."
                _toastMessage.value = "☀️ 아침 점호 완료! $statusMsg\n$resultMsg"
            } else {
                _toastMessage.value = "🌊 $resultMsg"
            }
        }
    }

    // 치트용 식량 보충 함수
    fun refillSupplies() {
        viewModelScope.launch {
            val currentShip = repository.ship.first() ?: return@launch
            val refilledShip = currentShip.copy(supplies = currentShip.maxSupplies)
            repository.saveShip(refilledShip)
            _toastMessage.postValue("🍞 식량을 가득 채웠습니다! (치트)")
        }
    }
}