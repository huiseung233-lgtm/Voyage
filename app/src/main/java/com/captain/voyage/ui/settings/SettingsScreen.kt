package com.captain.voyage.ui.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.captain.voyage.ui.theme.VoyageTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showResetDialog by remember { mutableStateOf(false) }

    VoyageTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5DC)) // Paper background
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "⚙️ 조타실 설정",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = Color(0xFF3E2723),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // 1. Time Settings
            SettingsSection(title = "⏰ 시간 규칙 설정") {
                // Limit Time
                TimeSettingRow(
                    label = "하루 마감 (정박) 시간",
                    desc = "이 시간 전까지는 '어제'로 칩니다.",
                    timeStr = uiState.limitTime,
                    onTimeClick = {
                        showTimePicker(context, uiState.limitTime) { newTime ->
                            viewModel.updateTimeSetting(limitTime = newTime)
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 12.dp))

                // Wake Time
                TimeSettingRow(
                    label = "하루 시작 (기상) 시간",
                    desc = "이 시간부터 출항이 가능합니다.",
                    timeStr = uiState.wakeTime,
                    onTimeClick = {
                        showTimePicker(context, uiState.wakeTime) { newTime ->
                            viewModel.updateTimeSetting(wakeTime = newTime)
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 12.dp))

                // Morning Buffer
                NumberInputRow(
                    label = "아침 점호 여유 시간",
                    desc = "기상 후 몇 분까지 지각 면제?",
                    value = uiState.morningBuffer,
                    unit = "분",
                    onValueChange = { viewModel.updateTimeSetting(buffer = it) }
                )
            }

            // 2. Notification Settings
            SettingsSection(title = "🔔 정기 보고 알림") {
                // Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "알림 켜기 (ON/OFF)",
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = uiState.isNotiEnabled,
                        onCheckedChange = { viewModel.updateNotificationSetting(isEnabled = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF6200EE),
                            checkedTrackColor = Color(0xFFBB86FC)
                        )
                    )
                }

                if (uiState.isNotiEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NumberInputRow(
                        label = "알림 간격 (분)",
                        desc = "얼마나 자주 보고하시겠습니까?",
                        value = uiState.notiInterval,
                        unit = "분 마다",
                        onValueChange = { viewModel.updateNotificationSetting(interval = it) }
                    )
                }
            }

            // 3. Danger Zone
            Column {
                Text(
                    text = "⚠️ 위험 구역",
                    color = Color(0xFF4E342E),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("모든 데이터 초기화 (Reset)", color = Color.White)
                }
            }
            
            // Padding at bottom
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("🚨 데이터 초기화") },
                text = { Text("정말 모든 데이터를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetAllData {
                                Toast.makeText(context, "초기화 완료", Toast.LENGTH_SHORT).show()
                                showResetDialog = false
                            }
                        }
                    ) {
                        Text("초기화", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = Color(0xFF4E342E),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun TimeSettingRow(
    label: String,
    desc: String,
    timeStr: String,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Text(text = desc, fontSize = 12.sp, color = Color(0xFF8D6E63))
        }
        OutlinedButton(onClick = onTimeClick) {
            Text(text = timeStr, color = Color(0xFF6200EE))
        }
    }
}

@Composable
fun NumberInputRow(
    label: String,
    desc: String,
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    // 로컬 상태로 텍스트 관리 (입력 중에는 즉시 반영하지 않음)
    var textState by remember(value) { mutableStateOf(value.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Text(text = desc, fontSize = 12.sp, color = Color(0xFF8D6E63))
        }
        OutlinedTextField(
            value = textState,
            onValueChange = { str ->
                if (str.all { it.isDigit() }) {
                    textState = str
                }
            },
            modifier = Modifier
                .width(80.dp)
                .onFocusChanged { focusState ->
                    // 포커스를 잃었을 때 (다른 곳 터치 등) 최종 값 반영
                    if (!focusState.isFocused) {
                        val newValue = textState.toIntOrNull() ?: value
                        if (newValue != value) {
                            onValueChange(newValue)
                        }
                        // 만약 비어있다면 원래 값(또는 반영된 값)으로 다시 표시
                        textState = newValue.toString()
                    }
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { 
                focusManager.clearFocus() // 포커스를 해제하여 onFocusChanged 트리거
            }),
            singleLine = true
        )
        Text(
            text = unit,
            modifier = Modifier.padding(start = 8.dp),
            color = Color(0xFF3E2723)
        )
    }
}

fun showTimePicker(context: android.content.Context, current: String, onSelected: (String) -> Unit) {
    val parts = current.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(context, { _, hour, minute ->
        onSelected(String.format("%02d:%02d", hour, minute))
    }, h, m, true).show()
}
