package com.captain.voyage.data.repository

import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.model.Rule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val voyageDao: VoyageDao
) {
    /**
     * 모든 규칙 목록을 Flow로 반환합니다.
     */
    val allRules: Flow<List<Rule>> = voyageDao.getAllRules()

    /**
     * 새로운 규칙을 추가합니다.
     */
    suspend fun addRule(rule: Rule) {
        voyageDao.insertRule(rule)
    }

    /**
     * 기존 규칙을 업데이트합니다.
     */
    suspend fun updateRule(rule: Rule) {
        voyageDao.updateRule(rule)
    }

    /**
     * 규칙을 삭제합니다.
     */
    suspend fun deleteRule(rule: Rule) {
        voyageDao.deleteRule(rule)
    }

    /**
     * 규칙의 표시 순서를 일괄 업데이트합니다.
     */
    suspend fun updateRulesOrder(rules: List<Rule>) {
        voyageDao.updateRulesOrder(rules)
    }
}
