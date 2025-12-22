package com.captain.voyage.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.repository.VoyageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted // Added
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map // Added
import kotlinx.coroutines.flow.stateIn // Added
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // 오늘 날짜 (고정)
    private val _todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // 1. 선택된 날짜 관리 (기본값: 오늘)
    private val _selectedDate = MutableLiveData(_todayDate)
    val selectedDate: LiveData<String> get() = _selectedDate

    // 화면 표시용 날짜
    private val _displayDate = MutableStateFlow("")
    val displayDate: StateFlow<String> = _displayDate.asStateFlow()

    // 목표 점수 (GoalType.DAILY 목표의 targetScore를 실시간으로 관찰)
    val targetScore: StateFlow<Int> = repository.allGoals
        .map { goals ->
            goals.find { it.type == GoalType.DAILY }?.targetScore ?: 100 // 없으면 기본 100
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    // ★ [New] 알림 클릭 시 로그북 팝업을 띄우기 위한 이벤트 신호
    private val _navigateToLogbook = MutableLiveData<Boolean>()
    val navigateToLogbook: LiveData<Boolean> get() = _navigateToLogbook

    init {
        val displayFormat = SimpleDateFormat("MM/dd (E)", Locale.getDefault())
        _displayDate.value = displayFormat.format(Date())
    }

    // ★ [New] 알림 클릭 시 MainActivity에서 호출하는 함수
    fun triggerLogbookPopup() {
        _navigateToLogbook.value = true
    }

    // ★ [New] Fragment에서 팝업을 띄운 후 신호를 소모하는 함수
    fun onLogbookPopupOpened() {
        _navigateToLogbook.value = false
    }

    // 날짜 선택 변경 함수
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // 2. 데이터 관찰
    val todayLog = repository.getLiveDailyLog(_todayDate).asLiveData()

    val selectedDateRecords: LiveData<List<ScoreRecord>> = _selectedDate.switchMap { date ->
        repository.getScoreRecordsByDate(date).asLiveData()
    }

    val allRules = repository.allRules.asLiveData()

    // 3. 기록 추가/삭제 기능
    // [수정] ruleId 파라미터 추가 (기본값 null)
    fun addRecord(targetDate: String, content: String, score: Int, ruleId: Long? = null) {
        viewModelScope.launch {
            val newRecord = ScoreRecord(
                date = targetDate,
                timestamp = System.currentTimeMillis(),
                ruleId = ruleId, // 여기서 저장해야 커스텀 목표에서 집계됨
                ruleTitle = content,
                score = score,
                note = "",
                isCustom = ruleId == null // ruleId가 없으면 완전 커스텀 입력
            )
            repository.insertScoreRecord(newRecord)
            updateTotalScore(targetDate, score)
        }
    }

    fun deleteRecord(record: ScoreRecord) {
        viewModelScope.launch {
            repository.deleteScoreRecord(record)
            updateTotalScore(record.date, -record.score)
        }
    }

    private suspend fun updateTotalScore(targetDate: String, deltaScore: Int) {
        val currentLog = repository.getDailyLogDirect(targetDate)
            ?: DailyLog(date = targetDate, totalScore = 0)

        val updatedLog = currentLog.copy(
            totalScore = currentLog.totalScore + deltaScore
        )
        repository.updateDailyLog(updatedLog)
    }

    // 4. 캘린더용 월간 데이터 로직 (실시간 Flow)
    private val _currentMonth = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    )

    val monthlyLogs: StateFlow<List<DailyLog>> = _currentMonth
        .flatMapLatest { yearMonth ->
            repository.getMonthlyLogs(yearMonth)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMonthlyLogs(yearMonth: String) {
        _currentMonth.value = yearMonth
    }
}