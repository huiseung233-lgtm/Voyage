package com.captain.voyage.ui.game

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.captain.voyage.data.model.PenaltyType
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.ui.theme.VoyageTheme

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onExit: () -> Unit
) {
    val ship by viewModel.ship.observeAsState()
    val userStatus by viewModel.userStatus.observeAsState()
    val toastMessage by viewModel.toastMessage.observeAsState()
    val context = LocalContext.current

    // Toast Message Handling
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Determine Colors based on status
    val isSailing = ship?.status == ShipStatus.SAILING
    val targetBackgroundColor = if (isSailing) Color(0xFF29B6F6) else Color(0xFF1A237E)
    val actionButtonColor = if (isSailing) Color(0xFF1565C0) else Color(0xFF4E342E)
    val actionButtonText = if (isSailing) "⚓ 정박하기 (하루 마감)" else "⛵ 출항하기"

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(1000), 
        label = "BgColor"
    )

    VoyageTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 1. HUD (Top Left)
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD7CCC8)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💰", fontSize = 18.sp)
                    Text(
                        text = "${userStatus?.gold ?: 0} G",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723),
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                    )
                    
                    // Separator
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFA1887F)))

                    // Status
                    val (icon, text, color) = when (userStatus?.penaltyType) {
                        PenaltyType.FATIGUE -> Triple("😫", "피로함", Color(0xFFF57F17))
                        PenaltyType.LAZINESS -> Triple("💤", "나태함", Color(0xFF5D4037))
                        PenaltyType.DOOM -> Triple("☠️", "위험!", Color(0xFFB71C1C))
                        else -> Triple("⛵", "순항 중", Color(0xFF1B5E20))
                    }

                    Text(icon, fontSize = 18.sp, modifier = Modifier.padding(start = 16.dp))
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // 2. Exit Button (Top Right)
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color(0xFFFFCCBC), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color(0xFFBF360C))
            }

            // 3. Shop (Only visible when Anchored)
            if (!isSailing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 60.dp, y = (-100).dp) // Approximate position from XML bias
                        .size(120.dp)
                        .background(Color(0x80FFEB3B), RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.refillSupplies()
                        },
                    contentAlignment = Alignment.Center
                ) {
                     // Label below handled by column or absolute positioning?
                     // In XML it was below. Let's put text inside for now.
                     Text(
                         text = "🏪 상점",
                         color = Color.White,
                         fontWeight = FontWeight.Bold,
                         modifier = Modifier
                             .align(Alignment.BottomCenter)
                             .background(Color(0x80000000))
                             .padding(4.dp)
                     )
                }
            }

            // 4. Action Button (Bottom)
            Button(
                onClick = { viewModel.toggleShipStatus() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionButtonColor)
            ) {
                Text(
                    text = actionButtonText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
