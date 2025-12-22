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
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.Ship
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WorldMapView(
    modifier: Modifier = Modifier,
    ports: List<Port>,
    ship: Ship?,
    onMapClick: ((Float, Float) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // [State]
    var isCameraLocked by remember { mutableStateOf(true) }
    var manualOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableStateOf(1f) }

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
    
    // [Colors]
    val oceanColor = Color(0xFF1A237E)
    val gridColor = Color(0x30FFFFFF)
    val shipColor = Color(0xFFFFD54F)
    val portColor = Color(0xFF4FC3F7)
    val routeColor = Color(0x80FFD54F)
    val textColor = Color.White
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
            .background(oceanColor)
            .fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
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
        ) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f

            if (isCameraLocked && ship != null) {
                val lockedX = width / 2f - ship.posX.toFloat()
                val lockedY = height / 2f - ship.posY.toFloat()
                manualOffset = Offset(lockedX, lockedY)
            }

            val camX = manualOffset.x
            val camY = manualOffset.y

            scale(scale = zoomScale, pivot = Offset(cx, cy)) {
                val worldMin = -5000f
                val worldMax = 5000f
                val gridSize = 60.dp.toPx()

                // Grid
                for (x in worldMin.toInt()..worldMax.toInt() step gridSize.toInt()) {
                    val screenX = x + camX
                    drawLine(gridColor, Offset(screenX, 0f), Offset(screenX, height), strokeWidth = 1f / zoomScale, pathEffect = gridPathEffect)
                }
                for (y in worldMin.toInt()..worldMax.toInt() step gridSize.toInt()) {
                    val screenY = y + camY
                    drawLine(gridColor, Offset(0f, screenY), Offset(width, screenY), strokeWidth = 1f / zoomScale, pathEffect = gridPathEffect)
                }

                // Route
                ship?.let {
                    if (it.destX != null && it.destY != null) {
                        val start = Offset(it.posX.toFloat() + camX, it.posY.toFloat() + camY)
                        val end = Offset(it.destX!!.toFloat() + camX, it.destY!!.toFloat() + camY)
                        drawLine(routeColor, start, end, strokeWidth = 2.dp.toPx() / zoomScale, pathEffect = routePathEffect)
                        val xSize = 6.dp.toPx() / zoomScale
                        drawLine(routeColor, Offset(end.x - xSize, end.y - xSize), Offset(end.x + xSize, end.y + xSize), 3f / zoomScale)
                        drawLine(routeColor, Offset(end.x - xSize, end.y + xSize), Offset(end.x + xSize, end.y - xSize), 3f / zoomScale)
                    }
                }

                // Ports
                ports.forEach { port ->
                    val px = port.posX.toFloat() + camX
                    val py = port.posY.toFloat() + camY
                    val center = Offset(px, py)
                    drawCircle(portColor.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = center)
                    drawCircle(portColor, radius = 6.dp.toPx(), center = center)
                    val textLayout = textMeasurer.measure(
                        text = port.name,
                        style = TextStyle(color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(px + 10.dp.toPx(), py - 12.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(textLayout.size.width.toFloat() + 8f, textLayout.size.height.toFloat() + 4f)
                    )
                    drawText(textLayoutResult = textLayout, topLeft = Offset(px + 14.dp.toPx(), py - 10.dp.toPx()))
                }

                // Ship
                ship?.let {
                    val shipScreenX = it.posX.toFloat() + camX
                    val shipScreenY = it.posY.toFloat() + camY
                    translate(left = shipScreenX, top = shipScreenY) {
                        rotate(degrees = shipRotation, pivot = Offset.Zero) {
                            drawPath(path = shipPath, color = shipColor)
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
        
        // Re-center Button
        if (!isCameraLocked || zoomScale != 1f) {
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