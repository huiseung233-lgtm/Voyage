package com.captain.voyage.ui.game

import android.content.Intent
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Import 추가
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.captain.voyage.ui.trade.TradeDialog
import com.captain.voyage.ui.trade.MarketItemUi
import com.captain.voyage.ui.settlement.SettlementDialog
import com.captain.voyage.ui.game.InventoryDialog
import com.captain.voyage.ui.animation.SailingScene
import com.captain.voyage.ui.animation.ViewMode
import kotlin.math.atan2

import com.captain.voyage.ui.home.CommonLogbookDialog // Import 추가
import com.captain.voyage.data.model.ScoreRecord // Import 추가
import java.time.LocalDate // Import 추가

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onExit: () -> Unit
) {
    val ship by viewModel.ship.observeAsState()
    val userStatus by viewModel.userStatus.observeAsState()
    val toastMessage by viewModel.toastMessage.observeAsState()
    val isAtPort by viewModel.isAtPort.collectAsState()
    
    val showBriefing by viewModel.showBriefing.collectAsState()
    val briefingData by viewModel.briefingData.collectAsState()
    
    val showMarketDialog by viewModel.showMarketDialog.collectAsState()
    val marketItems by viewModel.marketItems.collectAsState()
    
    val showSettlementDialog by viewModel.showSettlementDialog.collectAsState()
    val currentPortId by viewModel.currentPortId.collectAsState()
    val currentPort by viewModel.currentPort.collectAsState()
    
    val showInventoryDialog by viewModel.showInventoryDialog.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    
    val allPorts by viewModel.allPorts.collectAsState()
    
    // [New] 정박 전 확인 및 로그북 상태
    var showDockConfirmation by remember { mutableStateOf(false) }
    var showLogbookDialog by remember { mutableStateOf(false) }
    
    // [New] 로그북 데이터 로딩 상태
    var logbookInitialRecords by remember { mutableStateOf<List<ScoreRecord>?>(null) }
    val rules by viewModel.allRules.observeAsState(emptyList())
    
    val context = LocalContext.current

    // Toast Message Handling
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // 데이터 로딩 대기 (팅김 방지)
    if (ship == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1A237E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("항해 준비 중...", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Determine Colors based on status
    val isSailing = ship?.status == ShipStatus.SAILING
    val targetBackgroundColor = if (isSailing) Color(0xFF29B6F6) else Color(0xFF1A237E)
    val actionButtonColor = if (isSailing) Color(0xFF1565C0) else Color(0xFF4E342E)
    
    // 버튼 문구 변경
    val actionButtonText = if (isSailing) "⚓ 당직에게 인계하기 (항해 일지 작성)" else "⛵ 출항하기 (항해 시작)"

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(1000), 
        label = "BgColor"
    )

    VoyageTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // 기본 배경
        ) {
            // ... (기존 배경 및 HUD 코드 생략 - 변경 없음)
            
            // 0. Sailing Animation Scene (Background)
            val rotation = ship?.let {
                if (it.destX != null && it.destY != null) {
                    val dx = it.destX!! - it.posX
                    val dy = it.destY!! - it.posY
                    (atan2(dy, dx) * 180 / Math.PI).toFloat() + 90f
                } else 0f
            } ?: 0f

            val isVisuallyMoving = isSailing || !isAtPort

            SailingScene(
                modifier = Modifier.fillMaxSize(),
                viewMode = ViewMode.ISOMETRIC, 
                shipRotation = rotation,
                isMoving = isVisuallyMoving,
                isAtPort = isAtPort && !isSailing 
            )

            // Overlay for status color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = 0.1f)) 
            )

            // 1. HUD (Top Left)
            Column(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                // Gold HUD
                Card(
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Supply HUD
                val supplies = ship?.supplies ?: 0.0
                val maxSupplies = ship?.maxSupplies ?: 1000.0
                val supplyRatio = (supplies / maxSupplies).toFloat().coerceIn(0f, 1f)
                val supplyColor = if (supplyRatio < 0.2f) Color(0xFFD32F2F) else Color(0xFF388E3C)

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🍞", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${supplies.toInt()} / ${maxSupplies.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = supplyColor
                        )
                    }
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

            // 3. Shop & Settlement & Inventory Buttons
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 140.dp, end = 24.dp)
                    .size(64.dp)
                    .background(Color(0xFF8D6E63), CircleShape)
                    .clickable { viewModel.openInventory() },
                contentAlignment = Alignment.Center
            ) {
                 Text("🎒", fontSize = 32.sp)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 220.dp, end = 24.dp)
                    .size(64.dp)
                    .background(Color(0xFF5D4037), CircleShape)
                    .clickable { 
                        context.startActivity(Intent(context, MapActivity::class.java))
                    },
                contentAlignment = Alignment.Center
            ) {
                 Text("🗺️", fontSize = 32.sp)
            }
            
            val supplies = ship?.supplies ?: 0.0
            if (!isSailing && (isAtPort || supplies <= 0.0)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp)
                        .offset(y = (-40).dp)
                ) {
                    GameMenuButton(
                        icon = "🏪",
                        label = "상점",
                        onClick = { viewModel.openMarket() }
                    )
                }

                if (currentPort?.canEstablishSettlement == true) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 32.dp)
                            .offset(y = (-40).dp)
                    ) {
                        GameMenuButton(
                            icon = "🏰",
                            label = "정착지",
                            onClick = { viewModel.openSettlement() }
                        )
                    }
                }
            }

            // 4. Action Button (Bottom)
            Button(
                onClick = { 
                    if (isSailing) {
                        // 정박 시 확인 다이얼로그 띄우기
                        showDockConfirmation = true
                    } else {
                        viewModel.toggleShipStatus() // 출항은 바로 진행
                    }
                },
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
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // [New] 정박 확인 다이얼로그
        if (showDockConfirmation) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDockConfirmation = false },
                title = { Text("⚓ 정박 준비") },
                text = { Text("정박하기 전에 오늘의 마지막 일지를 작성하시겠습니까?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDockConfirmation = false
                            viewModel.openLogbookForDocking() // 정박 프로세스 시작
                            showLogbookDialog = true // 로그북 팝업 열기
                        }
                    ) {
                        Text("예, 작성할게요")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDockConfirmation = false
                            viewModel.confirmDocking() // 바로 정박
                        }
                    ) {
                        Text("아니요, 바로 정박", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFFFFF8E1)
            )
        }
        
        // [New] 게임 내 로그북 팝업 (CommonLogbookDialog 재사용)
        if (showLogbookDialog) {
            val todayDate = LocalDate.now().toString()
            
            // 데이터 로딩 (한 번만 실행)
            LaunchedEffect(Unit) {
                logbookInitialRecords = viewModel.getRecordsDirect(todayDate)
            }

            if (logbookInitialRecords != null) {
                CommonLogbookDialog(
                    date = todayDate,
                    initialRecords = logbookInitialRecords!!,
                    rules = rules,
                    onDismiss = { showLogbookDialog = false },
                    onSave = { records ->
                        viewModel.saveBatchRecords(todayDate, records)
                        showLogbookDialog = false
                    }
                )
            } else {
                // 로딩 중 표시
                androidx.compose.ui.window.Dialog(onDismissRequest = { showLogbookDialog = false }) {
                     Box(modifier = Modifier.size(100.dp).background(Color.White, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator(color = Color(0xFF3E2723))
                     }
                }
            }
        }

        // [New] 아침 점호 다이얼로그
        if (showBriefing && briefingData != null) {
            DailyBriefingDialog(
                data = briefingData!!,
                onConfirm = { hasConfessed -> 
                    viewModel.confirmBriefingAndSail(hasConfessed) 
                },
                onDismiss = { viewModel.dismissBriefing() }
            )
        }
        
        // ... (나머지 다이얼로그 유지)
        
        // [New] 인벤토리 다이얼로그
        if (showInventoryDialog) {
            InventoryDialog(
                inventoryItems = inventoryItems,
                onUseItem = { itemId -> viewModel.loadSupply(itemId) },
                onDismiss = { viewModel.closeInventory() }
            )
        }
        
        // [New] 무역 다이얼로그
        if (showMarketDialog) {
            TradeDialog(
                portName = "항구 #${currentPortId ?: "Unknown"}", // [Fixed] Clearer string template
                gold = userStatus?.gold ?: 0,
                marketItems = marketItems,
                onBuy = { item, qty -> viewModel.buyItem(item, qty) },
                onSell = { item, qty -> viewModel.sellItem(item, qty) },
                onDismiss = { viewModel.closeMarket() }
            )
        }

        // [New] 정착지 다이얼로그
        if (showSettlementDialog) {
            SettlementDialog(
                portId = currentPortId ?: 0L, // [Fixed] Type mismatch: Long? ?: Long
                portName = "항구 #${currentPortId ?: "Unknown"}",
                onDismiss = { viewModel.closeSettlement() }
            )
        }
    }
}

@Composable
fun GameMenuButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        androidx.compose.material3.Surface(
            shape = CircleShape,
            color = Color(0xFFFFF8E1), // Cream color
            shadowElevation = 6.dp,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 32.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    blurRadius = 4f
                )
            )
        )
    }
}