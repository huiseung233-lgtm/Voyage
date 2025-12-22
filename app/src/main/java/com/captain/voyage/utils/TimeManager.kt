package com.captain.voyage.utils

import java.time.LocalTime
import java.time.LocalDate

// 시간대별 상태 정의
enum class TimeState {
    NIGHT_PASSAGE, // 마감 시간 전 (야간 통행) -> '어제'로 기록
    MAINTENANCE,   // 마감 ~ 기상 (정비 시간) -> 출항 불가
    MORNING_CALL,  // 기상 ~ +여유시간 (아침 점호) -> 상쾌한 아침
    DAY_WORK       // 그 외 (일과 시간) -> 늦은 출항 시 패널티
}

object TimeManager {

    // ★ [설정] 사용자가 설정 가능한 시간 변수
    var LIMIT_TIME: LocalTime = LocalTime.of(2, 0)
    var WAKE_UP_TIME: LocalTime = LocalTime.of(7, 0)

    // ★ [설정] 점호 여유 시간 (분 단위, 기본 60분)
    var MORNING_BUFFER_MINUTES: Long = 60

    // 지각 판단 기준 (기상 시간 + 여유 시간)
    private val LATE_LIMIT: LocalTime
        get() = WAKE_UP_TIME.plusMinutes(MORNING_BUFFER_MINUTES)

    // 1. 현재 상태 반환 (순서가 중요합니다!)
    fun getCurrentState(): TimeState {
        val now = LocalTime.now()

        // (1) 정비 시간인가? (마감 ~ 기상)
        if (isBetween(LIMIT_TIME, WAKE_UP_TIME, now)) {
            return TimeState.MAINTENANCE
        }

        // (2) 아침 점호 시간인가? (기상 ~ 기상+여유시간)
        if (isBetween(WAKE_UP_TIME, LATE_LIMIT, now)) {
            return TimeState.MORNING_CALL
        }

        // (3) 야간 통행인가? (일과 시간이지만 날짜가 '어제'로 잡히는 경우)
        // 로직: 정비도 아니고 아침도 아닌데, 계산된 날짜가 '어제'라면 야간 통행임.
        if (getEffectiveDate() != LocalDate.now().toString()) {
            return TimeState.NIGHT_PASSAGE
        }

        // (4) 그 외에는 모두 일반 일과
        return TimeState.DAY_WORK
    }

    // 2. 유효 날짜 반환 (핵심 수정: 기상 시간을 기준으로 하루를 나눔)
    fun getEffectiveDate(): String {
        val nowTime = LocalTime.now()
        val nowDate = LocalDate.now()

        // "지금 시간이 내 기상 시간보다 이르다면, 아직 난 어제 살고 있는 것이다."
        // 예: 기상 07:00인데 지금 02:00 -> 어제 / 기상 20:00인데 지금 10:00 -> 어제
        return if (nowTime.isBefore(WAKE_UP_TIME)) {
            nowDate.minusDays(1).toString()
        } else {
            nowDate.toString()
        }
    }

    // 3. 출항 가능 여부 체크
    fun canSail(): Boolean {
        return getCurrentState() != TimeState.MAINTENANCE
    }

    // 4. 아침 점호 판정
    fun isMorningSailing(): Boolean {
        return getCurrentState() == TimeState.MORNING_CALL
    }

    // (내부 함수) 시간 범위 체크 헬퍼 (자정을 넘기는 경우도 완벽 지원)
    private fun isBetween(start: LocalTime, end: LocalTime, target: LocalTime): Boolean {
        if (start.isBefore(end)) {
            // 일반적인 경우 (예: 08:00 ~ 20:00)
            return (target == start || target.isAfter(start)) && target.isBefore(end)
        } else {
            // 자정을 넘기는 경우 (예: 22:00 ~ 06:00)
            // 타겟 시간이 시작 시간(22:00) 이후이거나, 종료 시간(06:00) 이전이면 포함
            return target.isAfter(start) || target.isBefore(end) || target == start
        }
    }
}