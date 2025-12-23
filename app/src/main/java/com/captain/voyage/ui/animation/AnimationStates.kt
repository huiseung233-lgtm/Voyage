package com.captain.voyage.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue

enum class ViewMode {
    ISOMETRIC, // 메인 화면용 (입체)
    TOP_DOWN   // 지도 배경용 (평면)
}

/**
 * 항해 애니메이션의 모든 동적 수치를 관리합니다.
 */
class SailingState(
    val waveOffset: Float,    // 파도의 움직임 총량
    val cloudOffset: Float,   // 구름의 이동 총량
    val shipBobbing: Float,   // 배의 흔들림
    val foamAlpha: Float      // 포말의 투명도
)

@Composable
fun rememberSailingState(): SailingState {
    val infiniteTransition = rememberInfiniteTransition(label = "SailingTransition")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing), // 속도 증가 (더 역동적으로)
            repeatMode = RepeatMode.Restart
        ), label = "Wave"
    )

    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing), // 구름도 조금 더 빠르게
            repeatMode = RepeatMode.Restart
        ), label = "Cloud"
    )

    val shipBobbing by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Bobbing"
    )

    val foamAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Foam"
    )

    return remember(waveOffset, cloudOffset, shipBobbing, foamAlpha) {
        SailingState(waveOffset, cloudOffset, shipBobbing, foamAlpha)
    }
}