package com.captain.voyage.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.repository.GoalRepository
import com.captain.voyage.data.repository.VoyageRepository
import com.captain.voyage.data.repository.WorldRepository // Added
import com.captain.voyage.utils.NotificationHelper
import com.captain.voyage.utils.TimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class SettingsState(
    val limitTime: String = "02:00",
    val wakeTime: String = "07:00",
    val morningBuffer: Int = 60,
    val isNotiEnabled: Boolean = false,
    val notiInterval: Int = 60,
    val isQuietHoursEnabled: Boolean = false,
    val quietStartTime: String = "23:00",
    val quietEndTime: String = "07:00",
    val isOverlayEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voyageRepository: VoyageRepository,
    private val goalRepository: GoalRepository,
    private val worldRepository: WorldRepository // Added
) : ViewModel() {

    private val PREF_NAME = "voyage_settings"
    private val KEY_LIMIT_TIME = "limit_time"
    private val KEY_WAKE_TIME = "wake_time"
    private val KEY_MORNING_BUFFER = "morning_buffer"
    private val KEY_NOTI_ENABLED = "noti_enabled"
    private val KEY_NOTI_INTERVAL = "noti_interval"
    private val KEY_QUIET_ENABLED = "quiet_enabled"
    private val KEY_QUIET_START = "quiet_start"
    private val KEY_QUIET_END = "quiet_end"
    
    // [New] Key for Overlay
    private val KEY_OVERLAY_ENABLED = "overlay_enabled"

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val limitStr = prefs.getString(KEY_LIMIT_TIME, "02:00") ?: "02:00"
        val wakeStr = prefs.getString(KEY_WAKE_TIME, "07:00") ?: "07:00"
        val bufferMin = prefs.getInt(KEY_MORNING_BUFFER, 60)
        val isNotiEnabled = prefs.getBoolean(KEY_NOTI_ENABLED, false)
        val notiInterval = prefs.getInt(KEY_NOTI_INTERVAL, 60)
        val isQuietEnabled = prefs.getBoolean(KEY_QUIET_ENABLED, false)
        val quietStart = prefs.getString(KEY_QUIET_START, "23:00") ?: "23:00"
        val quietEnd = prefs.getString(KEY_QUIET_END, "07:00") ?: "07:00"
        
        // [New] Load Overlay Setting
        val isOverlayEnabled = prefs.getBoolean(KEY_OVERLAY_ENABLED, false)

        _uiState.update {
            it.copy(
                limitTime = limitStr,
                wakeTime = wakeStr,
                morningBuffer = bufferMin,
                isNotiEnabled = isNotiEnabled,
                notiInterval = notiInterval,
                isQuietHoursEnabled = isQuietEnabled,
                quietStartTime = quietStart,
                quietEndTime = quietEnd,
                isOverlayEnabled = isOverlayEnabled
            )
        }

        updateTimeManager(limitStr, wakeStr, bufferMin)
    }

    fun updateTimeSetting(limitTime: String? = null, wakeTime: String? = null, buffer: Int? = null) {
        val current = _uiState.value
        val newLimit = limitTime ?: current.limitTime
        val newWake = wakeTime ?: current.wakeTime
        val newBuffer = buffer ?: current.morningBuffer

        prefs.edit().apply {
            putString(KEY_LIMIT_TIME, newLimit)
            putString(KEY_WAKE_TIME, newWake)
            putInt(KEY_MORNING_BUFFER, newBuffer)
            apply()
        }

        _uiState.update {
            it.copy(limitTime = newLimit, wakeTime = newWake, morningBuffer = newBuffer)
        }

        updateTimeManager(newLimit, newWake, newBuffer)
    }

    fun updateNotificationSetting(isEnabled: Boolean? = null, interval: Int? = null) {
        val current = _uiState.value
        val newEnabled = isEnabled ?: current.isNotiEnabled
        val newInterval = interval ?: current.notiInterval

        prefs.edit().apply {
            putBoolean(KEY_NOTI_ENABLED, newEnabled)
            putInt(KEY_NOTI_INTERVAL, newInterval)
            apply()
        }

        _uiState.update {
            it.copy(isNotiEnabled = newEnabled, notiInterval = newInterval)
        }

        if (newEnabled) {
            NotificationHelper.scheduleNotification(context, newInterval)
        } else {
            NotificationHelper.cancelNotification(context)
        }
    }
    
    fun updateQuietHours(isEnabled: Boolean? = null, startTime: String? = null, endTime: String? = null) {
        val current = _uiState.value
        val newEnabled = isEnabled ?: current.isQuietHoursEnabled
        val newStart = startTime ?: current.quietStartTime
        val newEnd = endTime ?: current.quietEndTime
        
        prefs.edit().apply {
            putBoolean(KEY_QUIET_ENABLED, newEnabled)
            putString(KEY_QUIET_START, newStart)
            putString(KEY_QUIET_END, newEnd)
            apply()
        }
        
        _uiState.update {
            it.copy(
                isQuietHoursEnabled = newEnabled,
                quietStartTime = newStart,
                quietEndTime = newEnd
            )
        }
    }
    
    fun updateOverlaySetting(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, isEnabled).apply()
        _uiState.update {
            it.copy(isOverlayEnabled = isEnabled)
        }
    }

    private fun updateTimeManager(limitStr: String, wakeStr: String, buffer: Int) {
        try {
            TimeManager.LIMIT_TIME = LocalTime.parse(limitStr)
            TimeManager.WAKE_UP_TIME = LocalTime.parse(wakeStr)
            TimeManager.MORNING_BUFFER_MINUTES = buffer.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            com.captain.voyage.data.local.VoyageDatabase.getDatabase(context).clearAllTables()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun cheatResetDaily(onResult: (String) -> Unit) {
        viewModelScope.launch {
            // 1. 로그 삭제 (GoalRepo)
            val today = LocalDate.now().toString()
            goalRepository.deleteDailyLogByDate(today)
            
            // 2. 선박 리셋 (VoyageRepo)
            val msg = voyageRepository.resetShipStatus()
            onResult("🔄 오늘 하루 리셋 완료! (로그 삭제 + $msg)")
        }
    }

    fun cheatAddGold(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val status = voyageRepository.userStatus.first()
            if (status != null) {
                val newStatus = status.copy(gold = status.gold + 100000)
                voyageRepository.saveUserStatus(newStatus)
                onResult("💰 100,000 골드가 지급되었습니다! (현재: ${newStatus.gold}G)")
            } else {
                onResult("❌ 유저 정보를 찾을 수 없습니다.")
            }
        }
    }

    fun cheatRevealMap(onResult: (String) -> Unit) {
        viewModelScope.launch {
            worldRepository.revealAllMap()
            onResult("🗺️ 전 세계의 지도가 밝혀졌습니다!")
        }
    }
}
