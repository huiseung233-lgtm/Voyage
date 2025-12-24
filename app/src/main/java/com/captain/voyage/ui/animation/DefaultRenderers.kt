package com.captain.voyage.ui.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

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

/**
 * [New] 이미지 기반 선박 렌더러 (8방향 스프라이트)
 * 사용법: SailingScene에서 8개의 ImageBitmap을 로드하여 주입합니다.
 * 순서: N(0), NE(1), E(2), SE(3), S(4), SW(5), W(6), NW(7)
 */
class ImageShipRenderer(
    private val shipImages: List<ImageBitmap>
) : ShipRenderer {
    override fun draw(drawScope: DrawScope, state: SailingState, rotation: Float, isMoving: Boolean, viewMode: ViewMode) {
        if (shipImages.isEmpty()) return

        // 1. 각도를 8방향 인덱스로 변환 (0~360 -> 0~7)
        // 0도(N)를 기준으로 45도씩 분할. 337.5~22.5도는 0번 인덱스.
        val normalizedRot = (rotation % 360 + 360) % 360
        val index = ((normalizedRot + 22.5f) / 45f).toInt() % 8
        
        // 안전 장치: 인덱스가 범위 밖이면 0번 사용
        val image = shipImages.getOrElse(index) { shipImages[0] }

        with(drawScope) {
            // 배의 흔들림 (Bobbing)
            translate(0f, state.shipBobbing) {
                // 이미지는 회전시키지 않고 원본 그대로 그립니다 (이미 각도가 반영된 이미지이므로)
                // 단, 중심점을 맞추기 위해 오프셋 조정
                val left = -image.width / 2
                val top = -image.height / 2
                
                drawImage(
                    image = image,
                    dstOffset = IntOffset(left, top)
                )

                // 이동 중일 때 물거품 효과 (이미지 아래쪽에 살짝)
                if (isMoving) {
                    val foamAlpha = state.foamAlpha
                    drawCircle(
                        color = Color.White.copy(alpha = foamAlpha * 0.5f),
                        radius = 10.dp.toPx(),
                        center = Offset(0f, image.height / 2f - 10f) // 배 꼬리 부분 추정
                    )
                }
            }
        }
    }
}