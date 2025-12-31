package com.captain.voyage.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.core.content.ContextCompat
import com.captain.voyage.ui.theme.VoyageTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showResetDialog by remember { mutableStateOf(false) }

    // 알림 권한 요청 런처
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.updateNotificationSetting(isEnabled = true)
        } else {
            Toast.makeText(context, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            viewModel.updateNotificationSetting(isEnabled = false)
        }
    }

    VoyageTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5DC))
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
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.updateNotificationSetting(isEnabled = true)
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    viewModel.updateNotificationSetting(isEnabled = true)
                                }
                            } else {
                                viewModel.updateNotificationSetting(isEnabled = false)
                            }
                        },
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
                    
                    HorizontalDivider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚫 방해 금지 시간",
                                color = Color(0xFF3E2723),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "설정된 시간에는 알림이 울리지 않습니다.",
                                fontSize = 12.sp,
                                color = Color(0xFF8D6E63)
                            )
                        }
                        Switch(
                            checked = uiState.isQuietHoursEnabled,
                            onCheckedChange = { viewModel.updateQuietHours(isEnabled = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFD32F2F),
                                checkedTrackColor = Color(0xFFFFCDD2)
                            )
                        )
                    }

                    if (uiState.isQuietHoursEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TimeSettingRow(
                            label = "금지 시작",
                            desc = "알림 차단 시작 시간",
                            timeStr = uiState.quietStartTime,
                            onTimeClick = {
                                showTimePicker(context, uiState.quietStartTime) { newTime ->
                                    viewModel.updateQuietHours(startTime = newTime)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeSettingRow(
                            label = "금지 종료",
                            desc = "알림 차단 해제 시간",
                            timeStr = uiState.quietEndTime,
                            onTimeClick = {
                                showTimePicker(context, uiState.quietEndTime) { newTime ->
                                    viewModel.updateQuietHours(endTime = newTime)
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 12.dp))

                    // [New] 하이브리드 팝업 설정 (오버레이)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ 팝업으로 바로 띄우기",
                                color = Color(0xFF3E2723),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "다른 앱 사용 중에도 즉시 점수를 입력합니다.",
                                fontSize = 12.sp,
                                color = Color(0xFF8D6E63)
                            )
                        }
                        Switch(
                            checked = uiState.isOverlayEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        // 권한이 없으면 설정 화면으로 이동
                                        Toast.makeText(context, "다른 앱 위에 그리기 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                        // 체크를 꺼둠 (사용자가 돌아와서 다시 켜야 함)
                                        viewModel.updateOverlaySetting(false)
                                    } else {
                                        viewModel.updateOverlaySetting(true)
                                    }
                                } else {
                                    viewModel.updateOverlaySetting(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00796B),
                                checkedTrackColor = Color(0xFFB2DFDB)
                            )
                        )
                    }
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
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.cheatResetDaily { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("🔄 [치트] 오늘 하루 리셋 (점호 가능)", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.cheatAddGold { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                ) {
                    Text("💰 [치트] 10만 골드 지급", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

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
                    if (!focusState.isFocused) {
                        val newValue = textState.toIntOrNull() ?: value
                        if (newValue != value) {
                            onValueChange(newValue)
                        }
                        textState = newValue.toString()
                    }
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { 
                focusManager.clearFocus()
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
