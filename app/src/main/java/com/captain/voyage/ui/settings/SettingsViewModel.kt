package com.captain.voyage.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captain.voyage.VoyageApplication
import com.captain.voyage.utils.NotificationHelper
import com.captain.voyage.utils.TimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

data class SettingsState(
    val limitTime: String = "02:00",
    val wakeTime: String = "07:00",
    val morningBuffer: Int = 60,
    val isNotiEnabled: Boolean = false,
    val notiInterval: Int = 60
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val PREF_NAME = "voyage_settings"
    private val KEY_LIMIT_TIME = "limit_time"
    private val KEY_WAKE_TIME = "wake_time"
    private val KEY_MORNING_BUFFER = "morning_buffer"
    private val KEY_NOTI_ENABLED = "noti_enabled"
    private val KEY_NOTI_INTERVAL = "noti_interval"

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

        _uiState.update {
            it.copy(
                limitTime = limitStr,
                wakeTime = wakeStr,
                morningBuffer = bufferMin,
                isNotiEnabled = isNotiEnabled,
                notiInterval = notiInterval
            )
        }

        // 초기 실행 시 TimeManager 동기화
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
            // Hilt 주입으로 Database에 직접 접근하지 않고, Repository나 UseCase를 통해야 하지만,
            // 지금은 임시로 ApplicationContext를 캐스팅하거나 해야 함.
            // 하지만 Database 인스턴스를 주입받지 않았으므로 여기서는 VoyageDatabase.getDatabase(context)를 호출.
            com.captain.voyage.data.local.VoyageDatabase.getDatabase(context).clearAllTables()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
