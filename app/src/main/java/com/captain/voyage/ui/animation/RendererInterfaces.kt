package com.captain.voyage.ui.animation

import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * 바다를 그리는 렌더러 인터페이스
 */
interface SeaRenderer {
    fun draw(drawScope: DrawScope, state: SailingState, isMoving: Boolean, shipRotation: Float, viewMode: ViewMode)
}

/**
 * 날씨(구름, 비 등)를 그리는 렌더러 인터페이스
 */
interface WeatherRenderer {
    fun draw(drawScope: DrawScope, state: SailingState, shipRotation: Float)
}

/**
 * 배를 그리는 렌더러 인터페이스
 */
interface ShipRenderer {
    fun draw(drawScope: DrawScope, state: SailingState, rotation: Float, isMoving: Boolean, viewMode: ViewMode)
}

/**
 * 항구(부두)를 그리는 렌더러 인터페이스
 */
interface PortRenderer {
    fun draw(drawScope: DrawScope, viewMode: ViewMode)
}