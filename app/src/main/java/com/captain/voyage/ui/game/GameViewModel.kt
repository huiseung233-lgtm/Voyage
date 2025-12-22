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

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // 1. 실시간 데이터 관찰
    val ship = repository.ship.asLiveData()
    val userStatus = repository.userStatus.asLiveData()

    // 2. UI 알림을 위한 LiveData
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    // ★ [수정됨] skinId를 문자열("0")로 변경하여 타입 에러 해결
    init {
        viewModelScope.launch {
            val currentShip = repository.ship.first()
            if (currentShip == null) {
                val defaultShip = Ship(
                    id = 1,
                    name = "나의 황금선",
                    level = 1,
                    exp = 0,
                    skinId = "0", // 여기가 숫자 0에서 문자열 "0"으로 바뀌었습니다!
                    status = ShipStatus.ANCHORED // 기본값 정박으로 변경
                )
                repository.saveShip(defaultShip)
            } else {
                // 앱 재실행 시, 만약 항해 중 상태라면 루프 재시작 (Repository에 위임)
                if (currentShip.status == ShipStatus.SAILING) {
                    repository.startVoyage()
                }
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
            val updatedShip = currentShip.copy(status = ShipStatus.SAILING)
            repository.saveShip(updatedShip)
            repository.startVoyage() // 중앙 엔진 가동!

            if (isMorning) {
                _toastMessage.value = "☀️ 좋은 아침입니다! 성실 보너스를 획득했습니다!"
            } else {
                _toastMessage.value = "🌊 안전하게 재출항합니다."
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