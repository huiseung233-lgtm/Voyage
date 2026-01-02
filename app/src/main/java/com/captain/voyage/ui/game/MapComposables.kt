package com.captain.voyage.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.captain.voyage.data.initial.LandShape
import com.captain.voyage.data.initial.WorldData
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.Ship
import com.captain.voyage.utils.GameConstants
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class MapTheme(
    val oceanColor: Color,
    val gridColor: Color,
    val shipColor: Color,
    val portColor: Color,
    val routeColor: Color,
    val textColor: Color
)

val DigitalTheme = MapTheme(
    oceanColor = Color(0xFF1A237E),
    gridColor = Color(0x30FFFFFF),
    shipColor = Color(0xFFFFD54F),
    portColor = Color(0xFF4FC3F7),
    routeColor = Color(0x80FFD54F),
    textColor = Color.White
)

val PaperTheme = MapTheme(
    oceanColor = Color(0xFFE0C9A6), // 양피지 베이지
    gridColor = Color(0x203E2723),  // 연한 갈색
    shipColor = Color(0xFFB71C1C),  // 붉은 잉크
    portColor = Color(0xFF3E2723),  // 짙은 갈색
    routeColor = Color(0x803E2723), // 갈색 경로
    textColor = Color(0xFF3E2723)
)

@Composable
fun WorldMapView(
    modifier: Modifier = Modifier,
    ports: List<Port>,
    ship: Ship?,
    isReadOnly: Boolean = false,
    initialZoom: Float = 1f,
    isPaperMap: Boolean = false,
    onMapClick: ((Float, Float) -> Unit)? = null,
    exploredChunks: Set<Pair<Int, Int>> = emptySet() // [New] Fog Data
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // [Theme Selection]
    val theme = if (isPaperMap) PaperTheme else DigitalTheme

    // [State]
    var isCameraLocked by remember { mutableStateOf(true) }
    var manualOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableStateOf(initialZoom) }

    // [Visual Assets]
    val shipPath = remember(density) {
        with(density) {
            Path().apply {
                moveTo(0f, -15.dp.toPx())
                lineTo(-10.dp.toPx(), 10.dp.toPx())
                lineTo(0f, 5.dp.toPx())
                lineTo(10.dp.toPx(), 10.dp.toPx())
                close()
            }
        }
    }
    
    val arrowPath = remember(density) {
        with(density) {
            Path().apply {
                moveTo(0f, -10.dp.toPx())
                lineTo(-8.dp.toPx(), 8.dp.toPx())
                lineTo(0f, 4.dp.toPx())
                lineTo(8.dp.toPx(), 8.dp.toPx())
                close()
            }
        }
    }
    
    val gridPathEffect = remember(density) {
        with(density) {
            PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f)
        }
    }
    
    val routePathEffect = remember(density) {
        with(density) {
            PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 10.dp.toPx()), 0f)
        }
    }
    
    // [Colors] - Removed local vals, use theme.xxx
    val indicatorColor = Color(0xFFFF5252)

    // [Rotation]
    val targetRotation = ship?.let {
        if (it.destX != null && it.destY != null) {
            val dx = it.destX!! - it.posX
            val dy = it.destY!! - it.posY
            (atan2(dy, dx) * 180 / Math.PI).toFloat() + 90f
        } else {
            0f
        }
    } ?: 0f

    val shipRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 500),
        label = "ShipRotation"
    )

    // [Layout & Camera]
    Box(
        modifier = modifier
            .background(theme.oceanColor)
            .fillMaxSize()
    ) {
        val inputModifier = if (isReadOnly) Modifier else Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    isCameraLocked = false
                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3f)
                    manualOffset += pan / zoomScale 
                }
            }
            .pointerInput(ship, isCameraLocked, manualOffset, zoomScale) {
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                
                val currentCamX = if (isCameraLocked && ship != null) {
                    width / 2f - ship.posX.toFloat()
                } else {
                    manualOffset.x
                }
                val currentCamY = if (isCameraLocked && ship != null) {
                    height / 2f - ship.posY.toFloat()
                } else {
                    manualOffset.y
                }

                detectTapGestures { offset ->
                    val cx = width / 2f
                    val cy = height / 2f
                    val relX = offset.x - cx
                    val relY = offset.y - cy
                    val unscaledRelX = relX / zoomScale
                    val unscaledRelY = relY / zoomScale
                    val unscaledScreenX = unscaledRelX + cx
                    val unscaledScreenY = unscaledRelY + cy
                    val worldX = unscaledScreenX - currentCamX
                    val worldY = unscaledScreenY - currentCamY
                    onMapClick?.invoke(worldX, worldY)
                }
            }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(inputModifier)
        ) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f

            // ReadOnly 모드면 항상 카메라 잠금 (배 추적)
            val effectiveLocked = isReadOnly || isCameraLocked

            if (effectiveLocked && ship != null) {
                val lockedX = width / 2f - ship.posX.toFloat()
                val lockedY = height / 2f - ship.posY.toFloat()
                manualOffset = Offset(lockedX, lockedY)
            }

            val camX = manualOffset.x
            val camY = manualOffset.y

            scale(scale = zoomScale, pivot = Offset(cx, cy)) {
                val worldMin = GameConstants.WORLD_MIN.toFloat()
                val worldMax = GameConstants.WORLD_MAX.toFloat()
                val gridSize = 100.dp.toPx()

                // 1. Grid
                for (x in worldMin.toInt()..worldMax.toInt() step gridSize.toInt()) {
                    val screenX = x + camX
                    drawLine(theme.gridColor, Offset(screenX, worldMin + camY), Offset(screenX, worldMax + camY), strokeWidth = 1f / zoomScale, pathEffect = gridPathEffect)
                }
                for (y in worldMin.toInt()..worldMax.toInt() step gridSize.toInt()) {
                    val screenY = y + camY
                    drawLine(theme.gridColor, Offset(worldMin + camX, screenY), Offset(worldMax + camX, screenY), strokeWidth = 1f / zoomScale, pathEffect = gridPathEffect)
                }

                // 2. Landmasses (New)
                WorldData.landmasses.forEach { shape ->
                    when (shape) {
                        is LandShape.Circle -> {
                            val centerX = shape.centerX.toFloat() + camX
                            val centerY = shape.centerY.toFloat() + camY
                            val radius = shape.radius.toFloat()
                            val landColor = if (isPaperMap) Color(0xFF8B7355) else Color(0xFF2E7D32)
                            drawCircle(
                                color = landColor,
                                radius = radius,
                                center = Offset(centerX, centerY)
                            )
                        }
                    }
                }

                // [New] Fog of War Layer
                val worldChunkSize = 100.0 // 월드 좌표상 청크 크기
                
                // 1. 현재 화면에 보이는 월드 좌표 범위 계산 (줌 고려)
                val worldViewWidth = width / zoomScale
                val worldViewHeight = height / zoomScale
                
                // 화면 중앙(cx, cy)의 월드 좌표: cx - camX
                val worldCenterX = cx - camX
                val worldCenterY = cy - camY
                
                val worldLeft = worldCenterX - (worldViewWidth / 2)
                val worldRight = worldCenterX + (worldViewWidth / 2)
                val worldTop = worldCenterY - (worldViewHeight / 2)
                val worldBottom = worldCenterY + (worldViewHeight / 2)

                // 2. 보이는 범위 내의 청크 인덱스 계산
                val minChunkX = (worldLeft / worldChunkSize).toInt() - 1
                val maxChunkX = (worldRight / worldChunkSize).toInt() + 1
                val minChunkY = (worldTop / worldChunkSize).toInt() - 1
                val maxChunkY = (worldBottom / worldChunkSize).toInt() + 1

                for (cx_idx in minChunkX..maxChunkX) {
                    for (cy_idx in minChunkY..maxChunkY) {
                        // 탐험되지 않은 청크라면 검은색 덮기
                        if (!exploredChunks.contains(cx_idx to cy_idx)) {
                            // scale 블록 안이므로 월드 좌표 + camX 만 해주면 됨
                            val drawX = (cx_idx * worldChunkSize).toFloat() + camX
                            val drawY = (cy_idx * worldChunkSize).toFloat() + camY
                            
                            drawRect(
                                color = Color.Black.copy(alpha = 0.98f),
                                topLeft = Offset(drawX, drawY),
                                size = androidx.compose.ui.geometry.Size(worldChunkSize.toFloat(), worldChunkSize.toFloat())
                            )
                        }
                    }
                }

                // 3. Map Boundary (Red Line)
                val borderMin = worldMin + camX
                val borderMax = worldMax + camX
                val borderTop = worldMin + camY
                val borderBottom = worldMax + camY
                
                // Top
                drawLine(Color.Red, Offset(borderMin, borderTop), Offset(borderMax, borderTop), strokeWidth = 10f / zoomScale)
                // Bottom
                drawLine(Color.Red, Offset(borderMin, borderBottom), Offset(borderMax, borderBottom), strokeWidth = 10f / zoomScale)
                // Left
                drawLine(Color.Red, Offset(borderMin, borderTop), Offset(borderMin, borderBottom), strokeWidth = 10f / zoomScale)
                // Right
                drawLine(Color.Red, Offset(borderMax, borderTop), Offset(borderMax, borderBottom), strokeWidth = 10f / zoomScale)

                // 4. Route
                ship?.let {
                    if (it.destX != null && it.destY != null) {
                        val start = Offset(it.posX.toFloat() + camX, it.posY.toFloat() + camY)
                        val end = Offset(it.destX!!.toFloat() + camX, it.destY!!.toFloat() + camY)
                        drawLine(theme.routeColor, start, end, strokeWidth = 2.dp.toPx() / zoomScale, pathEffect = routePathEffect)
                        val xSize = 6.dp.toPx() / zoomScale
                        drawLine(theme.routeColor, Offset(end.x - xSize, end.y - xSize), Offset(end.x + xSize, end.y + xSize), 3f / zoomScale)
                        drawLine(theme.routeColor, Offset(end.x - xSize, end.y + xSize), Offset(end.x + xSize, end.y - xSize), 3f / zoomScale)
                    }
                }

                // Ports
                ports.forEach { port ->
                    val px = port.posX.toFloat() + camX
                    val py = port.posY.toFloat() + camY
                    val center = Offset(px, py)
                    drawCircle(theme.portColor.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = center)
                    drawCircle(theme.portColor, radius = 6.dp.toPx(), center = center)
                    val textLayout = textMeasurer.measure(
                        text = port.name,
                        style = TextStyle(color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    // 배경 사각형 없이 텍스트만 (깔끔하게) - 또는 반투명 배경
                    if (!isPaperMap) {
                         drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(px + 10.dp.toPx(), py - 12.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(textLayout.size.width.toFloat() + 8f, textLayout.size.height.toFloat() + 4f)
                        )
                    }
                    drawText(textLayoutResult = textLayout, topLeft = Offset(px + 14.dp.toPx(), py - 10.dp.toPx()))
                }

                // Ship
                ship?.let {
                    val shipScreenX = it.posX.toFloat() + camX
                    val shipScreenY = it.posY.toFloat() + camY
                    translate(left = shipScreenX, top = shipScreenY) {
                        rotate(degrees = shipRotation, pivot = Offset.Zero) {
                            drawPath(path = shipPath, color = theme.shipColor)
                        }
                    }
                }
            }

            // Off-screen Indicator
            ship?.let {
                val rawScreenX = it.posX.toFloat() + camX
                val rawScreenY = it.posY.toFloat() + camY
                val zoomedScreenX = (rawScreenX - cx) * zoomScale + cx
                val zoomedScreenY = (rawScreenY - cy) * zoomScale + cy
                val isOffScreen = zoomedScreenX < 0 || zoomedScreenX > width || zoomedScreenY < 0 || zoomedScreenY > height
                if (isOffScreen) {
                    val padding = 30.dp.toPx()
                    val indicatorX = zoomedScreenX.coerceIn(padding, width - padding)
                    val indicatorY = zoomedScreenY.coerceIn(padding, height - padding)
                    val dx = zoomedScreenX - cx
                    val dy = zoomedScreenY - cy
                    val angleRad = atan2(dy, dx)
                    val angleDeg = (angleRad * 180 / Math.PI).toFloat() + 90f
                    translate(left = indicatorX, top = indicatorY) {
                        rotate(degrees = angleDeg, pivot = Offset.Zero) {
                            drawPath(path = arrowPath, color = indicatorColor)
                        }
                    }
                }
            }
        }
        
        // Re-center Button (Hide in ReadOnly Mode)
        if (!isReadOnly && (!isCameraLocked || zoomScale != 1f)) {
            IconButton(
                onClick = { 
                    isCameraLocked = true 
                    zoomScale = 1f
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.White, CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Re-center",
                    tint = Color.Black
                )
            }
        }
    }
}