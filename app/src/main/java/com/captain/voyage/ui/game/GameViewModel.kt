package com.captain.voyage.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.repository.VoyageRepository
import com.captain.voyage.utils.TimeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(private val repository: VoyageRepository) : ViewModel() {

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
                    status = ShipStatus.SAILING
                )
                repository.saveShip(defaultShip)
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
            val updatedShip = currentShip.copy(status = ShipStatus.ANCHORED)
            repository.saveShip(updatedShip)

            // 안전하게 toString() 유지
            val effectiveDate = TimeManager.getEffectiveDate().toString()
            _toastMessage.value = "⚓ 정박 완료! ($effectiveDate 기록 마감)"
        }
    }

    private fun sailShip(currentShip: Ship) {
        if (!TimeManager.canSail()) {
            _toastMessage.value = "⛔ 지금은 선박 정비 시간(02:00~07:00)입니다. 출항할 수 없습니다."
            return
        }

        val isMorning = TimeManager.isMorningSailing()

        viewModelScope.launch {
            val updatedShip = currentShip.copy(status = ShipStatus.SAILING)
            repository.saveShip(updatedShip)

            if (isMorning) {
                _toastMessage.value = "☀️ 좋은 아침입니다! 성실 보너스를 획득했습니다!"
            } else {
                _toastMessage.value = "🌊 안전하게 재출항합니다."
            }
        }
    }
}

class GameViewModelFactory(private val repository: VoyageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}