package com.captain.voyage.ui.goals

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // Added
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults // Added
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.GoalType
import com.captain.voyage.data.model.Rule
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val goalsUiState by viewModel.goalsUiState.collectAsState()
    val allRules by viewModel.allRules.collectAsState()

    var showBigMap by remember { mutableStateOf(false) }
    
    // Dialog States
    var showSimpleDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) } // 수정/생성 대상
    var editingType by remember { mutableStateOf(GoalType.WEEKLY) } // 다이얼로그 타입

    // --- Main Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7CCC8))
    ) {
        // 1. Map Banner (Top 40%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(Color(0xFF8D6E63)) // 지도 배경색
                .clickable { showBigMap = true },
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for Map Image
            Text(
                text = "🗺️ World Map (Tap to Expand)",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2. Goals List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // (0) Daily Goal
            val dailyGoal = goalsUiState.find { it.goal.type == GoalType.DAILY }
            item {
                GoalItemCard(
                    title = "Daily Target",
                    goalUi = dailyGoal,
                    onTap = {
                        editingGoal = dailyGoal?.goal
                        editingType = GoalType.DAILY
                        showSimpleDialog = true
                    },
                    onDelete = { }
                )
            }

            // (1) Weekly Goal
            val weeklyGoal = goalsUiState.find { it.goal.type == GoalType.WEEKLY }
            item {
                GoalItemCard(
                    title = "Weekly Milestone",
                    goalUi = weeklyGoal,
                    onTap = {
                        editingGoal = weeklyGoal?.goal
                        editingType = GoalType.WEEKLY
                        showSimpleDialog = true
                    },
                    onDelete = { }
                )
            }

            // (2) Monthly Goal
            val monthlyGoal = goalsUiState.find { it.goal.type == GoalType.MONTHLY }
            item {
                GoalItemCard(
                    title = "Monthly Legend",
                    goalUi = monthlyGoal,
                    onTap = {
                        editingGoal = monthlyGoal?.goal
                        editingType = GoalType.MONTHLY
                        showSimpleDialog = true
                    },
                    onDelete = { }
                )
            }

            // (3) Custom Goals
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Captain's Code (Custom)", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                    IconButton(onClick = {
                        editingGoal = null // 새 목표
                        showCustomDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom Goal", tint = Color(0xFF3E2723))
                    }
                }
            }

            items(goalsUiState.filter { it.goal.type == GoalType.CUSTOM }) { uiState ->
                GoalItemCard(
                    title = uiState.goal.title.ifBlank { "Custom Goal" }, // 제목 표시
                    goalUi = uiState,
                    onTap = {
                        editingGoal = uiState.goal
                        showCustomDialog = true
                    },
                    onDelete = { viewModel.deleteGoal(uiState.goal) },
                    showDelete = true,
                    subtitle = "${uiState.goal.startDate} ~ ${uiState.goal.endDate}"
                )
            }
            
            // 하단 여백
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // --- Dialogs ---

    if (showBigMap) {
        BigMapDialog(onDismiss = { showBigMap = false })
    }

    if (showSimpleDialog) {
        SimpleGoalDialog(
            type = editingType,
            initialScore = editingGoal?.targetScore ?: 100,
            onDismiss = { showSimpleDialog = false },
            onConfirm = { score ->
                viewModel.addOrUpdateGoal(
                    type = editingType,
                    title = if (editingType == GoalType.DAILY) "Daily Target" else "",
                    targetScore = score,
                    existingGoalId = editingGoal?.id ?: 0
                )
                showSimpleDialog = false
            }
        )
    }

    if (showCustomDialog) {
        CustomGoalDialog(
            initialGoal = editingGoal,
            allRules = allRules,
            onDismiss = { showCustomDialog = false },
            onConfirm = { title, score, start, end, ruleIds ->
                viewModel.addOrUpdateGoal(
                    type = GoalType.CUSTOM,
                    title = title,
                    targetScore = score,
                    startDate = start,
                    endDate = end,
                    ruleIds = ruleIds,
                    existingGoalId = editingGoal?.id ?: 0
                )
                showCustomDialog = false
            }
        )
    }
}

// --- Components ---

@Composable
fun GoalItemCard(
    title: String,
    goalUi: GoalUiState?,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean = false,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                    if (subtitle != null) {
                        Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                if (showDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            if (goalUi == null) {
                Text("목표 설정이 필요합니다. 터치하여 설정하세요.", fontSize = 14.sp, color = Color.Gray)
            } else {
                val progress = goalUi.progress
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = if (progress >= 1f) Color(0xFF4CAF50) else Color(0xFFFFD700),
                    trackColor = Color(0xFFE0E0E0),
                )
                Text(
                    text = "${goalUi.currentScore} / ${goalUi.goal.targetScore} P",
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
            }
        }
    }
}

@Composable
fun BigMapDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }, // 클릭하면 닫힘
            contentAlignment = Alignment.Center
        ) {
            Text("🗺️ Big Map View", color = Color.White, fontSize = 30.sp)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun SimpleGoalDialog(
    type: GoalType,
    initialScore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var scoreStr by remember { mutableStateOf(initialScore.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "${type.name} 목표 설정", color = Color(0xFF3E2723)) },
        text = {
            OutlinedTextField(
                value = scoreStr,
                onValueChange = { if (it.all { c -> c.isDigit() }) scoreStr = it },
                label = { Text("목표 점수") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(scoreStr.toIntOrNull() ?: 0) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = Color(0xFF5D4037)) }
        },
        containerColor = Color(0xFFFFF8E1)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomGoalDialog(
    initialGoal: Goal?,
    allRules: List<Rule>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String, List<Long>) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialGoal?.title ?: "") } // Added
    var scoreStr by remember { mutableStateOf(initialGoal?.targetScore?.toString() ?: "100") }
    var startDate by remember { mutableStateOf(initialGoal?.startDate ?: LocalDate.now().toString()) }
    var endDate by remember { mutableStateOf(initialGoal?.endDate ?: LocalDate.now().plusDays(7).toString()) }
    var searchQuery by remember { mutableStateOf("") }
    
    // 선택된 룰 ID들
    var selectedRuleIds by remember { 
        mutableStateOf(initialGoal?.linkedRuleIds?.toSet() ?: emptySet()) 
    }

    // 날짜 선택 함수
    fun showDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
        val date = LocalDate.parse(currentDate)
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newDate = LocalDate.of(year, month + 1, day)
                onDateSelected(newDate.toString())
            },
            date.year, date.monthValue - 1, date.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) "커스텀 목표 생성" else "커스텀 목표 수정", color = Color(0xFF3E2723)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 0. 제목 (Added)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("목표 이름 (예: 다이어트)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. 점수
                OutlinedTextField(
                    value = scoreStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) scoreStr = it },
                    label = { Text("목표 점수") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 2. 날짜 (Clickable ReadOnly TextField)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f).clickable { showDatePicker(startDate) { startDate = it } }) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {},
                            label = { Text("시작일") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            ),
                            trailingIcon = { Icon(Icons.Default.DateRange, null) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f).clickable { showDatePicker(endDate) { endDate = it } }) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {},
                            label = { Text("종료일") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            ),
                            trailingIcon = { Icon(Icons.Default.DateRange, null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // 3. 룰 검색 및 리스트
                Text("규칙 선택", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("규칙 검색...") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    trailingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                // 4. 스크롤 가능한 리스트 (높이 제한)
                LazyColumn(
                    modifier = Modifier
                        .height(200.dp) // 높이 고정
                        .background(Color.White, RoundedCornerShape(4.dp))
                ) {
                    val filteredRules = if (searchQuery.isBlank()) allRules else allRules.filter {
                        it.title.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredRules) { rule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRuleIds = if (selectedRuleIds.contains(rule.id)) {
                                        selectedRuleIds - rule.id
                                    } else {
                                        selectedRuleIds + rule.id
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedRuleIds.contains(rule.id),
                                onCheckedChange = null // Row 클릭으로 처리
                            )
                            Text(text = rule.title, fontSize = 14.sp, color = Color.Black)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(title, scoreStr.toIntOrNull() ?: 0, startDate, endDate, selectedRuleIds.toList()) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = Color(0xFF5D4037)) }
        },
        containerColor = Color(0xFFFFF8E1)
    )
}