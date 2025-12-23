package com.captain.voyage.ui.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 기본 바다 (잔잔한 파도)
 */
class DefaultSeaRenderer : SeaRenderer {
    override fun draw(drawScope: DrawScope, state: SailingState, isMoving: Boolean, shipRotation: Float, viewMode: ViewMode) {
        with(drawScope) {
            val oceanColor = Color(0xFF1A237E)
            val waveColor = Color(0x60FFFFFF) 

            drawRect(color = oceanColor)

            val speedMultiplier = if (isMoving) 1.0f else 0.1f
            val gridSize = 120.dp.toPx()
            
            // 배의 진행 방향 반대(+180도)로 파도가 흐름
            val flowAngleRad = Math.toRadians((shipRotation - 90 + 180).toDouble())
            val totalOffset = state.waveOffset * speedMultiplier
            val offsetX = (totalOffset * cos(flowAngleRad)).toFloat() % gridSize
            val offsetY = (totalOffset * sin(flowAngleRad)).toFloat() % gridSize
            
            for (x in -gridSize.toInt()..size.width.toInt() + gridSize.toInt() step gridSize.toInt()) {
                for (y in -gridSize.toInt()..size.height.toInt() + gridSize.toInt() step gridSize.toInt()) {
                    val drawX = x + offsetX
                    val drawY = y + offsetY
                    
                    val path = Path().apply {
                        moveTo(drawX - 10.dp.toPx(), drawY)
                        quadraticBezierTo(drawX, drawY + 5.dp.toPx(), drawX + 10.dp.toPx(), drawY)
                    }
                    drawPath(path = path, color = waveColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                }
            }
        }
    }
}

/**
 * 기본 날씨 (맑음 + 구름 조금)
 */
class DefaultWeatherRenderer : WeatherRenderer {
    override fun draw(drawScope: DrawScope, state: SailingState, shipRotation: Float) {
        with(drawScope) {
            val cloudColor = Color.White.copy(alpha = 0.2f)
            val cloudSize = Size(180.dp.toPx(), 70.dp.toPx())
            
            val flowAngleRad = Math.toRadians((shipRotation - 90 + 180).toDouble())
            
            val cloudX = (state.cloudOffset * cos(flowAngleRad)).toFloat() % (size.width + cloudSize.width)
            val cloudY = (state.cloudOffset * sin(flowAngleRad)).toFloat() % (size.height + cloudSize.height)
            
            val finalX = if (cloudX < -cloudSize.width) size.width + cloudX else cloudX
            val finalY = if (cloudY < -cloudSize.height) size.height + cloudY else cloudY

            drawOval(
                color = cloudColor,
                topLeft = Offset(finalX, finalY + 100.dp.toPx()),
                size = cloudSize
            )
        }
    }
}

/**
 * 기본 선박 (노란색)
 */
class DefaultShipRenderer : ShipRenderer {
    override fun draw(drawScope: DrawScope, state: SailingState, rotation: Float, isMoving: Boolean, viewMode: ViewMode) {
        with(drawScope) {
            val shipColor = Color(0xFFFFD54F)
            val foamColor = Color.White.copy(alpha = state.foamAlpha)

            // 배의 흔들림 적용
            translate(0f, state.shipBobbing) {
                rotate(rotation, pivot = Offset.Zero) {
                    val path = Path().apply {
                        moveTo(0f, -25.dp.toPx()) 
                        lineTo(-15.dp.toPx(), 10.dp.toPx())
                        lineTo(0f, 5.dp.toPx()) 
                        lineTo(15.dp.toPx(), 10.dp.toPx()) 
                        close()
                    }
                    
                    if (isMoving) {
                        for (i in 1..3) {
                            val alpha = state.foamAlpha * (1f - i * 0.25f)
                            drawCircle(
                                color = Color.White.copy(alpha = alpha),
                                radius = (15 / i).dp.toPx(),
                                center = Offset(0f, (25 + i * 15).dp.toPx())
                            )
                        }
                    }
                    
                    drawPath(path = path, color = shipColor)
                    drawCircle(color = Color(0xFF3E2723), radius = 3.dp.toPx(), center = Offset.Zero)
                }
            }
        }
    }
}

/**
 * 기본 항구 (갈색 부두)
 */
class DefaultPortRenderer : PortRenderer {
    override fun draw(drawScope: DrawScope, viewMode: ViewMode) {
        with(drawScope) {
            val dockColor = Color(0xFF5D4037)
            drawRect(
                color = dockColor,
                topLeft = Offset(-100.dp.toPx(), -60.dp.toPx()),
                size = Size(40.dp.toPx(), 120.dp.toPx())
            )
            drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = Offset(-80.dp.toPx(), -40.dp.toPx()))
            drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = Offset(-80.dp.toPx(), 40.dp.toPx()))
        }
    }
}