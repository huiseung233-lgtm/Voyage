package com.captain.voyage.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.repository.VoyageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repository: VoyageRepository
) : ViewModel() {

    // 1. 검색어 상태 (기본값: 빈 문자열)
    private val _searchQuery = MutableStateFlow("")

    // 2. 규칙 리스트 (DB 데이터 + 검색어 필터링)
    val rules: StateFlow<List<Rule>> = combine(
        repository.allRules, // DB에서 오는 전체 데이터
        _searchQuery         // 현재 검색어
    ) { allRules, query ->
        if (query.isBlank()) {
            allRules // 검색어 없으면 다 보여줌
        } else {
            // 검색어 있으면 제목에 포함된 것만 필터링 (대소문자 무시)
            allRules.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- 기능 함수들 ---

    // 검색어 입력 시 호출
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // [수정된 addRule 함수]
    fun addRule(title: String, description: String, reward: Int, penalty: Int) {
        viewModelScope.launch {
            val newRule = Rule(
                title = title,
                description = description,
                defaultScore = reward,
                penalty = penalty,
                type = com.captain.voyage.data.model.RuleType.DAILY, // 기본값
                orderIndex = 0 // 일단 0으로 넣고 나중에 정렬
            )
            repository.addRule(newRule)
        }
    }

    // 규칙 삭제
    fun deleteRule(rule: Rule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    // 규칙 순서 변경 저장 (Drag & Drop 후 호출)
    fun updateRulesOrder(updatedList: List<Rule>) {
        viewModelScope.launch {
            // 리스트의 순서대로 index를 다시 매겨서 DB 업데이트
            updatedList.forEachIndexed { index, rule ->
                if (rule.orderIndex != index) {
                    repository.updateRule(rule.copy(orderIndex = index))
                }
            }
        }
    }

    fun updateRule(rule: Rule) {
        viewModelScope.launch {
            repository.updateRule(rule)
        }
    }
}