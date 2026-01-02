package com.captain.voyage.utils

object GameConstants {
    // --- Economy ---
    const val SETTLEMENT_FOUND_COST = 1000L
    const val BUILDING_BASE_COST = 500L
    
    // --- Sailing ---
    const val DAILY_MOVE_SUCCESS = 100000.0 // Extreme Speed for testing
    const val DAILY_MOVE_FAIL = 50000.0
    const val DRIFT_MOVE = 0.0

    // --- World ---
    const val MAP_SIZE = 20000.0 // -10000 to +10000
    const val WORLD_MIN = -10000.0
    const val WORLD_MAX = 10000.0
    
    // --- Navigation ---
    const val PORT_DETECTION_RADIUS = 50.0 // 항구 진입 인정 반경
    const val SNAP_RADIUS = 40.0 // 항로 설정 시 자석 효과 반경
    const val ARRIVAL_THRESHOLD = 1.0 // 목적지 도착 인정 오차

    // --- Supply ---
    const val SUPPLY_CONSUMPTION_DAILY = 10.0 // 하루 식량 소모량
    const val DEFAULT_MAX_SUPPLY = 1000.0 // [Changed] 기본 최대 식량
}
