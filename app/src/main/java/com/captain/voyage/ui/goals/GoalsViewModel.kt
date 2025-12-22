package com.captain.voyage.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.repository.VoyageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class GoalUiState(
    val goal: Goal,
    val currentScore: Int = 0,
    val progress: Float = 0f
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // 모든 룰 (커스텀 목표 설정 시 선택용)
    val allRules: StateFlow<List<Rule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 모든 목표 + 각 목표별 진행도(Progress) 결합
    val goalsUiState: StateFlow<List<GoalUiState>> = repository.allGoals
        .flatMapLatest { goals ->
            if (goals.isEmpty()) {
                flowOf(emptyList())
            } else {
                // 각 Goal에 대해 getGoalProgress Flow를 결합
                val progressFlows = goals.map { goal ->
                    repository.getGoalProgress(goal).map { score ->
                        val current = score ?: 0
                        val ratio = if (goal.targetScore > 0) current.toFloat() / goal.targetScore else 0f
                        GoalUiState(goal, current, ratio.coerceIn(0f, 1f))
                    }
                }
                combine(progressFlows) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- 목표 생성/수정 ---

    fun addOrUpdateGoal(
        type: GoalType,
        title: String = "", // Added
        targetScore: Int,
        startDate: String? = null,
        endDate: String? = null,
        ruleIds: List<Long> = emptyList(),
        existingGoalId: Long = 0
    ) {
        // 날짜 자동 계산
        val (start, end) = when (type) {
            GoalType.DAILY -> Pair(LocalDate.now().toString(), LocalDate.now().toString()) // 매일 갱신되지만 일단 오늘로 저장
            GoalType.WEEKLY -> getWeeklyRange()
            GoalType.MONTHLY -> getMonthlyRange()
            GoalType.CUSTOM -> Pair(startDate ?: "", endDate ?: "")
        }

        viewModelScope.launch {
            // DAILY, WEEKLY, MONTHLY는 타입별로 하나만 존재해야 함 (기존 것 덮어쓰기 로직 필요할 수 있음)
            // 여기서는 id가 0이면 새로 만들지만, 이미 있으면 업데이트.
            // *Daily 목표는 앱 켜질 때 없으면 만드는 로직이 어딘가 있어야 함. (일단 여기서는 수정만)
            
            val goal = Goal(
                id = existingGoalId,
                title = if (title.isBlank()) type.name else title,
                type = type,
                targetScore = targetScore,
                startDate = start,
                endDate = end,
                linkedRuleIds = ruleIds
            )
            repository.saveGoal(goal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // --- 날짜 헬퍼 함수 ---
    private fun getWeeklyRange(): Pair<String, String> {
        val today = LocalDate.now()
        // 이번 주 월요일 ~ 일요일
        val start = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val end = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
        return Pair(start.toString(), end.toString())
    }

    private fun getMonthlyRange(): Pair<String, String> {
        val today = LocalDate.now()
        // 이번 달 1일 ~ 말일
        val start = today.with(TemporalAdjusters.firstDayOfMonth())
        val end = today.with(TemporalAdjusters.lastDayOfMonth())
        return Pair(start.toString(), end.toString())
    }
}
