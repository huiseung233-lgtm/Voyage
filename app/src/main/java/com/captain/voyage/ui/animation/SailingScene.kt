package com.captain.voyage.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.imageResource
import com.captain.voyage.R

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
    shipRenderer: ShipRenderer? = null, // [Changed] 기본값을 null로 변경하여 내부에서 생성 가능하게 함
    portRenderer: PortRenderer = DefaultPortRenderer()
) {
    val state = rememberSailingState()

    // 8방향 이미지 로드 (가정된 리소스 ID 사용)
    val shipImages = listOf(
        ImageBitmap.imageResource(R.drawable.ship_n),
        ImageBitmap.imageResource(R.drawable.ship_ne),
        ImageBitmap.imageResource(R.drawable.ship_e),
        ImageBitmap.imageResource(R.drawable.ship_se),
        ImageBitmap.imageResource(R.drawable.ship_s),
        ImageBitmap.imageResource(R.drawable.ship_sw),
        ImageBitmap.imageResource(R.drawable.ship_w),
        ImageBitmap.imageResource(R.drawable.ship_nw)
    )

    // 사용자가 외부에서 shipRenderer를 주입하지 않았다면, 이미지 기반 렌더러를 기본으로 사용
    val effectiveShipRenderer = shipRenderer ?: remember(shipImages) {
        ImageShipRenderer(shipImages)
    }

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
                    effectiveShipRenderer.draw(this, state, shipRotation, isMoving, viewMode)
                }
            }

            // 5. 날씨 그리기
            weatherRenderer.draw(this, state, shipRotation)
        }
    }
}