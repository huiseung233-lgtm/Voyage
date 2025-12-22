package com.captain.voyage.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.Ship

@Composable
fun WorldMapView(
    modifier: Modifier = Modifier,
    ports: List<Port>,
    ship: Ship?,
    onMapClick: ((Float, Float) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    // [Optimization] 렌더링 객체 캐싱
    val shipPath = remember(density) {
        with(density) {
            Path().apply {
                moveTo(0f, -15.dp.toPx()) // 위
                lineTo(-10.dp.toPx(), 10.dp.toPx()) // 좌하
                lineTo(10.dp.toPx(), 10.dp.toPx()) // 우하
                close()
            }
        }
    }
    
    val dashPathEffect = remember(density) {
        with(density) {
            PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 10.dp.toPx()), 0f)
        }
    }
    
    val gridColor = Color(0x40000000)
    val oceanColor = Color(0xFF81D4FA)
    val shipColor = Color(0xFFD32F2F)
    val portColor = Color(0xFF5D4037)

    Box(
        modifier = modifier
            .background(oceanColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onMapClick?.invoke(offset.x, offset.y)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 100.dp.toPx()
            val width = size.width
            val height = size.height

            // 1. 격자선 (Grid Lines)
            // 화면 밖의 격자선은 그리지 않는 최적화도 가능하지만, 현재는 전체 순회
            for (x in 0..width.toInt() step gridSize.toInt()) {
                drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), height), strokeWidth = 1f)
            }
            for (y in 0..height.toInt() step gridSize.toInt()) {
                drawLine(gridColor, Offset(0f, y.toFloat()), Offset(width, y.toFloat()), strokeWidth = 1f)
            }

            // 2-1. 목적지 경로 그리기
            ship?.let {
                if (it.destX != null && it.destY != null) {
                    val start = Offset(it.posX.toFloat(), it.posY.toFloat())
                    val end = Offset(it.destX!!.toFloat(), it.destY!!.toFloat())
                    
                    drawLine(
                        color = shipColor,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashPathEffect
                    )
                    drawCircle(
                        color = shipColor,
                        radius = 4.dp.toPx(),
                        center = end,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // 2. 항구(Ports) 그리기
            ports.forEach { port ->
                val center = Offset(port.posX.toFloat(), port.posY.toFloat())
                
                drawCircle(
                    color = portColor,
                    radius = 8.dp.toPx(),
                    center = center
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = port.name,
                    topLeft = center.copy(x = center.x + 12.dp.toPx(), y = center.y - 10.dp.toPx()),
                    style = TextStyle(color = Color.Black, fontSize = 12.sp)
                )
            }

            // 3. 배(Ship) 그리기
            ship?.let {
                translate(left = it.posX.toFloat(), top = it.posY.toFloat()) {
                    drawPath(path = shipPath, color = shipColor)
                }
            }
        }
    }
}
