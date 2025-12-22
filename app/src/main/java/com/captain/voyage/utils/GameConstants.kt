package com.captain.voyage.utils

object GameConstants {
    // --- Economy ---
    const val SETTLEMENT_FOUND_COST = 1000L
    const val BUILDING_BASE_COST = 500L
    
    // --- Sailing ---
    const val DAILY_MOVE_SUCCESS = 100.0
    const val DAILY_MOVE_FAIL = 50.0
    const val DRIFT_MOVE = 0.0
    
    // --- Navigation ---
    const val PORT_DETECTION_RADIUS = 50.0 // 항구 진입 인정 반경
    const val SNAP_RADIUS = 40.0 // 항로 설정 시 자석 효과 반경
    const val ARRIVAL_THRESHOLD = 1.0 // 목적지 도착 인정 오차
}
