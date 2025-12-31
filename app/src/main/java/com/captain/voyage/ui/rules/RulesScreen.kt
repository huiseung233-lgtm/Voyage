package com.captain.voyage.ui.rules

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.captain.voyage.R
import com.captain.voyage.data.model.Rule
import com.captain.voyage.ui.theme.VoyageBackgroundParchment
import com.captain.voyage.ui.theme.VoyageTheme
import com.captain.voyage.ui.theme.voyageTextFieldColors
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@Composable
fun RulesScreen(
    viewModel: RulesViewModel
) {
    val rules by viewModel.rules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<Rule?>(null) }
    var ruleToDelete by remember { mutableStateOf<Rule?>(null) }

    // [New] 로컬 상태 관리: 드래그 중에는 UI가 즉시 반응해야 하므로 로컬 state 사용
    var localRules by remember { mutableStateOf(rules) }
    
    // DB 데이터가 바뀌면 로컬 상태 동기화 (단, 드래그 중이 아닐 때만 하는 것이 좋지만, 간단히 동기화)
    LaunchedEffect(rules) {
        localRules = rules
    }

    VoyageTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF8D6E63),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Rule")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFD7CCC8)) // Background color from XML
            ) {
                // Header
                Text(
                    text = "📜 선박 규율 (Ship's Rules)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF5D4037))
                        .padding(16.dp),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Search Bar
                var searchQuery by remember { mutableStateOf("") }
                
                // [New] 검색어 업데이트 시 로직
                fun updateSearch(query: String) {
                    searchQuery = query
                    viewModel.setSearchQuery(query)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { updateSearch(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color(0xFFF5F5DC), RoundedCornerShape(4.dp)),
                    placeholder = { Text("규칙을 찾아보게...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                // [Fix] Reorderable State
                // 검색 중이 아닐 때만 드래그 가능하도록 설정
                val isReorderingEnabled = searchQuery.isBlank()

                // Rules List
                val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
                
                // [Fix] rememberReorderableLazyColumnState API usage
                val reorderableState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
                    // 1. UI 즉시 갱신 (순서 변경 로직)
                    localRules = localRules.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                    // 진동 피드백 등을 추가할 수 있음
                }
                
                // [Fix] 드래그 종료 감지 (onDragEnd가 없을 경우 LaunchedEffect로 처리하거나 onSettle 사용)
                // 2.x 버전에서는 state의 isDragging이 false가 되는 시점을 감지하여 저장하는 것이 안전함
                LaunchedEffect(reorderableState.isAnyItemDragging) {
                    if (!reorderableState.isAnyItemDragging) {
                        // 드래그가 끝났고, 순서가 변경되었다면 저장
                        // (단, 초기 로딩 시 호출 방지 및 실제 변경 확인 필요)
                        if (localRules != rules) {
                             viewModel.updateRulesOrder(localRules)
                        }
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(localRules, key = { it.id }) { rule ->
                        // [New] ReorderableItem Wrapper
                        ReorderableItem(reorderableState, key = rule.id) { isDragging ->
                            
                            val elevation = animateDpAsState(if (isDragging) 8.dp else 2.dp, label = "elevation")
                            
                            RuleItem(
                                rule = rule,
                                isDragging = isDragging,
                                isReorderingEnabled = isReorderingEnabled,
                                modifier = Modifier.shadow(elevation.value), // 카드 전체 스타일
                                handleModifier = Modifier.draggableHandle(), // 핸들 전용 modifier
                                onClick = {
                                    if (!isDragging) {
                                        editingRule = rule
                                        showAddDialog = true
                                    }
                                },
                                onDeleteClick = { ruleToDelete = rule }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom padding for FAB
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog) {
// ... (생략)
        }
    }
}

@Composable
fun RuleItem(
    rule: Rule,
    isDragging: Boolean = false,
    isReorderingEnabled: Boolean = true,
    modifier: Modifier = Modifier, // 카드 전체에 적용될 modifier
    handleModifier: Modifier = Modifier, // 드래그 핸들에만 적용될 modifier
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) Color(0xFFD7CCC8) else Color(0xFFF5F5DC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag Handle
            if (isReorderingEnabled) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_drag_handle),
                    contentDescription = "Drag to reorder",
                    tint = Color(0xFF8D6E63),
                    modifier = handleModifier.size(24.dp) // ★ 전달받은 draggableHandle 적용
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(36.dp)) 
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    color = Color(0xFF3E2723),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (rule.description.isNotBlank()) {
                    Text(
                        text = rule.description,
                        color = Color(0xFF4E342E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "+${rule.defaultScore} P | ${rule.penalty} P",
                    color = Color(0xFF5D4037),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete, // Or R.drawable.ic_delete
                    contentDescription = "Delete",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun RuleEditorDialog(
    rule: Rule?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf(rule?.title ?: "") }
    var description by remember { mutableStateOf(rule?.description ?: "") }
    var reward by remember { mutableStateOf(rule?.defaultScore ?: 10) }
    var penalty by remember { mutableStateOf(rule?.penalty ?: -10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoyageBackgroundParchment,
        title = { Text(if (rule == null) "📜 새로운 선박 규율" else "📜 규율 내용 수정", color = Color(0xFF3E2723)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("규율 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = voyageTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("상세 설명") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = voyageTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reward Stepper
                Text("보상 점수 (Reward)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { reward -= 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("-5", color = Color.White) }
                    Text(
                        text = "$reward",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = { reward += 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("+5", color = Color.White) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Penalty Stepper
                Text("패널티 점수 (Penalty)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { penalty -= 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("-5", color = Color.White) }
                    Text(
                        text = "$penalty",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = { penalty += 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("+5", color = Color.White) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, reward, penalty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Text(if (rule == null) "서명 ✍️" else "수정 완료", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color(0xFF5D4037))
            }
        }
    )
}