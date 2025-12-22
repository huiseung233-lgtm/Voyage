package com.captain.voyage.ui.settlement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.captain.voyage.data.model.BuildingType

@Composable
fun SettlementDialog(
    portId: Long,
    portName: String,
    onDismiss: () -> Unit,
    viewModel: SettlementViewModel = hiltViewModel()
) {
    LaunchedEffect(portId) {
        viewModel.loadSettlement(portId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    
    // 메시지 토스트 (간단히 구현)
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            // 실제 앱에서는 Snackbar 등을 사용 권장
            // 여기서는 뷰모델에서 메시지 처리를 완료했다고 가정하고 잠시 후 클리어
             kotlinx.coroutines.delay(2000)
             viewModel.clearMessage()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 전체 화면에 가깝게
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$portName 정착지",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.settlement == null) {
                    // 정착지 없음: 건설 화면
                    EmptySettlementView(
                        userGold = uiState.userGold,
                        onFound = { viewModel.foundSettlement(portId, "$portName 본부") }
                    )
                } else {
                    // 정착지 있음: 건물 목록
                    SettlementDashboard(
                        uiState = uiState,
                        onConstruct = { type -> 
                            uiState.settlement?.let { 
                                viewModel.constructBuilding(it.id, type) 
                            }
                        }
                    )
                }
                
                uiMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySettlementView(
    userGold: Long,
    onFound: () -> Unit
) {
    val COST = 1000
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("아직 이곳에 정착지가 없습니다.")
        Text("새로운 거점을 마련하여 영토를 확장하세요.")
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onFound,
            enabled = userGold >= COST
        ) {
            Text("정착지 건설 (${COST}G)")
        }
        if (userGold < COST) {
            Text(
                text = "골드가 부족합니다. (보유: ${userGold}G)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SettlementDashboard(
    uiState: SettlementUiState,
    onConstruct: (BuildingType) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Level ${uiState.settlement?.level}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "보유 골드: ${uiState.userGold}G",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val hq = uiState.buildings.find { it.type == BuildingType.HEADQUARTERS }
        val hqLevel = hq?.level ?: 0

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BuildingType.values()) { type ->
                val building = uiState.buildings.find { it.type == type }
                val currentLevel = building?.level ?: 0
                val nextLevel = currentLevel + 1
                val cost = nextLevel * 500
                
                // 건설 조건: 
                // 1. 돈 충분
                // 2. 본부 레벨 제한 (본부 자체는 제외)
                val isGoldEnough = uiState.userGold >= cost
                val isLevelAllowed = (type == BuildingType.HEADQUARTERS) || (nextLevel <= hqLevel)
                val canConstruct = isGoldEnough && isLevelAllowed

                Card(
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = type.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (currentLevel > 0) "Lv.$currentLevel" else "미건설",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if(currentLevel > 0) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                            Button(
                                onClick = { onConstruct(type) },
                                enabled = canConstruct
                            ) {
                                Text(if (currentLevel == 0) "건설 (${cost}G)" else "강화 (${cost}G)")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = type.description, style = MaterialTheme.typography.bodySmall)
                        
                        if (!isLevelAllowed) {
                            Text(
                                text = "본부 레벨(${hqLevel})을 초과할 수 없습니다.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
