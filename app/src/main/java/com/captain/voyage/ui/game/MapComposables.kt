package com.captain.voyage.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
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

    Box(
        modifier = modifier
            .background(Color(0xFF81D4FA)) // 바다 색상 (Light Blue)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onMapClick?.invoke(offset.x, offset.y)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. 격자선 (Grid Lines) 그리기
            val gridSize = 100.dp.toPx()
            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                drawLine(Color(0x40000000), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), strokeWidth = 1f)
            }
            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                drawLine(Color(0x40000000), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), strokeWidth = 1f)
            }

            // 2-1. 목적지 경로 그리기 (배 -> 목적지)
            ship?.let {
                if (it.destX != null && it.destY != null) {
                    drawLine(
                        color = Color(0xFFD32F2F),
                        start = Offset(it.posX.toFloat(), it.posY.toFloat()),
                        end = Offset(it.destX!!.toFloat(), it.destY!!.toFloat()),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    // 목적지 표시 X
                    drawCircle(
                        color = Color(0xFFD32F2F),
                        radius = 4.dp.toPx(),
                        center = Offset(it.destX!!.toFloat(), it.destY!!.toFloat()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // 2. 항구(Ports) 그리기
            ports.forEach { port ->
                val center = Offset(port.posX.toFloat(), port.posY.toFloat())
                
                // 항구 아이콘 (원)
                drawCircle(
                    color = Color(0xFF5D4037), // 갈색
                    radius = 8.dp.toPx(),
                    center = center
                )

                // 항구 이름 텍스트
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
                    val shipPath = Path().apply {
                        moveTo(0f, -15.dp.toPx()) // 위쪽 꼭짓점
                        lineTo(-10.dp.toPx(), 10.dp.toPx()) // 왼쪽 아래
                        lineTo(10.dp.toPx(), 10.dp.toPx()) // 오른쪽 아래
                        close()
                    }
                    drawPath(
                        path = shipPath,
                        color = Color(0xFFD32F2F) // 빨간색 배
                    )
                }
            }
        }
    }
}