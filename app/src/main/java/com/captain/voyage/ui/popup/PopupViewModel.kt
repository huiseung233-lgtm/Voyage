package com.captain.voyage.ui.popup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.data.repository.VoyageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PopupViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val rules: StateFlow<List<Rule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _initialRecords = MutableStateFlow<List<ScoreRecord>?>(null)
    val initialRecords: StateFlow<List<ScoreRecord>?> = _initialRecords.asStateFlow()

    init {
        loadTodayRecords()
    }

    private fun loadTodayRecords() {
        viewModelScope.launch {
            _initialRecords.value = repository.getScoreRecordsByDate(todayDate).first()
        }
    }
    
    // [New] 항해 상태 확인
    suspend fun isShipSailing(): Boolean {
        val currentShip = repository.ship.first()
        return currentShip?.status == ShipStatus.SAILING
    }

    fun saveBatchRecords(newRecords: List<ScoreRecord>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val oldRecords = repository.getScoreRecordsByDate(todayDate).first()
            oldRecords.forEach { repository.deleteScoreRecord(it) }

            var totalScore = 0
            newRecords.forEach { 
                repository.insertScoreRecord(it)
                totalScore += it.score
            }

            val log = DailyLog(date = todayDate, totalScore = totalScore)
            repository.updateDailyLog(log)
            
            onComplete()
        }
    }
}
