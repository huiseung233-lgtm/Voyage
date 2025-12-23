package com.captain.voyage.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate

@Composable
fun SailingScene(
    modifier: Modifier = Modifier,
    viewMode: ViewMode = ViewMode.ISOMETRIC,
    shipRotation: Float = 0f,
    isMoving: Boolean = false,
    isAtPort: Boolean = false,
    // [New] 렌더러 주입 (기본값: DefaultRenderers)
    seaRenderer: SeaRenderer = DefaultSeaRenderer(),
    weatherRenderer: WeatherRenderer = DefaultWeatherRenderer(),
    shipRenderer: ShipRenderer = DefaultShipRenderer(),
    portRenderer: PortRenderer = DefaultPortRenderer()
) {
    val state = rememberSailingState()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2

            // 1. 바다 그리기 (렌더러 위임)
            seaRenderer.draw(this, state, isMoving, shipRotation, viewMode)

            // 2. 카메라 변환 시작
            translate(cx, cy) {
                val effectiveScaleY = if (viewMode == ViewMode.ISOMETRIC) 0.7f else 1.0f
                
                scale(scaleX = 1f, scaleY = effectiveScaleY, pivot = Offset.Zero) {
                    // 3. 정박 중이라면 부두 그리기
                    if (isAtPort) {
                        portRenderer.draw(this, viewMode)
                    }

                    // 4. 배 그리기
                    shipRenderer.draw(this, state, shipRotation, isMoving, viewMode)
                }
            }

            // 5. 날씨 그리기
            weatherRenderer.draw(this, state, shipRotation)
        }
    }
}