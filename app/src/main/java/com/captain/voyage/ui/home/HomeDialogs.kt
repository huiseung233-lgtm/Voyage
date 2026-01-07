package com.captain.voyage.ui.home

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.captain.voyage.ui.theme.VoyageBackgroundParchment
import com.captain.voyage.ui.theme.voyageTextFieldColors

// --- Calendar Dialog ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDialog(
    viewModel: HomeViewModel,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthlyLogs by viewModel.monthlyLogs.collectAsState()

    LaunchedEffect(currentYearMonth) {
        val ymStr = currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.loadMonthlyLogs(ymStr)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                    }
                    Text(
                        text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val calendarDays = remember(currentYearMonth, monthlyLogs) {
                    generateCalendarDays(currentYearMonth, monthlyLogs)
                }
                LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(300.dp)) {
                    items(listOf("일", "월", "화", "수", "목", "금", "토")) { day ->
                        Text(text = day, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                    }
                    items(calendarDays) { day ->
                        CalendarDayItem(day, onDateSelected)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))) {
                    Text("닫기", color = Color.White)
                }
            }
        }
    }
}

data class CalendarDayUi(val date: String, val day: Int, val score: Int?, val isEmpty: Boolean)

@RequiresApi(Build.VERSION_CODES.O)
fun generateCalendarDays(ym: YearMonth, logs: List<DailyLog>): List<CalendarDayUi> {
    val daysInMonth = ym.lengthOfMonth()
    val firstDayOfMonth = ym.atDay(1)
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value
    val emptySlots = if (dayOfWeekValue == 7) 0 else dayOfWeekValue
    val list = mutableListOf<CalendarDayUi>()
    for (i in 0 until emptySlots) list.add(CalendarDayUi("", 0, null, true))
    for (day in 1..daysInMonth) {
        val dateStr = ym.atDay(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val log = logs.find { it.date == dateStr }
        list.add(CalendarDayUi(dateStr, day, log?.totalScore, false))
    }
    return list
}

@Composable
fun CalendarDayItem(day: CalendarDayUi, onClick: (String) -> Unit) {
    if (day.isEmpty) {
        Box(modifier = Modifier.size(40.dp))
    } else {
        val hasScore = day.score != null
        val scoreColor = when {
            (day.score ?: 0) >= 80 -> Color(0xFF4CAF50)
            (day.score ?: 0) >= 50 -> Color(0xFFFF9800)
            else -> Color(0xFF9E9E9E)
        }
        Column(
            modifier = Modifier.padding(2.dp).clickable { onClick(day.date) }.background(Color.White, RoundedCornerShape(4.dp)).padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "${day.day}", fontSize = 12.sp, color = Color.Black)
            if (hasScore) Box(modifier = Modifier.size(8.dp).background(scoreColor, CircleShape)) else Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

// --- Logbook Dialog (ViewModel Wrapper) ---

@Composable
fun LogbookDialog(
    viewModel: HomeViewModel,
    date: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val rules by viewModel.allRules.observeAsState(emptyList())
    var initialRecords by remember { mutableStateOf<List<ScoreRecord>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.setSelectedDate(date)
        initialRecords = viewModel.getRecordsDirect(date)
    }

    if (initialRecords != null) {
        CommonLogbookDialog(
            date = date,
            initialRecords = initialRecords!!,
            rules = rules,
            onDismiss = onDismiss,
            onSave = { records ->
                coroutineScope.launch {
                    val isSailing = viewModel.isShipSailing()
                    if (isSailing) {
                        viewModel.saveBatchRecords(date, records)
                        onDismiss()
                        Toast.makeText(context, "항해 일지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "⚓ 정박 중에는 일지를 쓸 수 없습니다. 먼저 출항해주세요!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
             Box(modifier = Modifier.size(100.dp).background(Color.White, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator(color = Color(0xFF3E2723))
             }
        }
    }
}

// --- Common Logbook Dialog ---
// Dialog Wrapper
@Composable
fun CommonLogbookDialog(
    date: String,
    initialRecords: List<ScoreRecord>,
    rules: List<Rule>,
    onDismiss: () -> Unit,
    onSave: (List<ScoreRecord>) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // [Refactor] Content extracted to LogbookContent
        // Dialog width/height constraints can be managed here or in content
        Box(
            modifier = Modifier.fillMaxWidth(0.95f).height(700.dp).padding(16.dp)
        ) {
            LogbookContent(
                date = date,
                initialRecords = initialRecords,
                rules = rules,
                onDismiss = onDismiss,
                onSave = onSave
            )
        }
    }
}

// --- Logbook Content (Reusable UI) ---
@Composable
fun LogbookContent(
    date: String,
    initialRecords: List<ScoreRecord>,
    rules: List<Rule>,
    onDismiss: () -> Unit,
    onSave: (List<ScoreRecord>) -> Unit
) {
    var tempRecords by remember { mutableStateOf(initialRecords) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📜 $date 항해 일지", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            
            // Total Score
            val dayScore = tempRecords.sumOf { it.score }
            Text(
                text = "기록 예정 합계 : ${if (dayScore > 0) "+" else ""}$dayScore P",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            HorizontalDivider(color = Color(0xFF8D6E63))

            // List
            Text("임시 기록 내역", modifier = Modifier.padding(top = 4.dp), fontSize = 11.sp, color = Color.Gray)
            LazyColumn(modifier = Modifier.height(135.dp).background(Color.White, RoundedCornerShape(4.dp)).padding(6.dp)) {
                items(tempRecords) { record ->
                    LogRecordItem(record) { 
                        // [New] 삭제 시 토스트 메시지
                        val msg = if (record.score >= 0) "-${record.score}점 (기록 삭제)" else "+${-record.score}점 (감점 기록 삭제)"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        tempRecords = tempRecords.filter { it !== record } 
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFF8D6E63))

            // Rule Selection & Search (Combined Row - Slim version)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Slim Search Field
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text("규칙 검색...", fontSize = 12.sp, color = Color.Gray)
                                }
                                innerTextField()
                            }
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                )
                
                Button(
                    onClick = { showAddCustomDialog = true },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("직접 입력", fontSize = 11.sp)
                }
            }
            
            LazyColumn(modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(4.dp)).padding(6.dp)) {
                val filteredRules = if (searchQuery.isBlank()) rules else rules.filter { it.title.contains(searchQuery, ignoreCase = true) }
                items(filteredRules) { rule ->
                    LogRuleItem(rule) { isSuccess ->
                        val score = if (isSuccess) rule.defaultScore else rule.penalty
                        val suffix = if (isSuccess) "" else " (실패)"
                        val newTempRecord = ScoreRecord(
                            date = date,
                            timestamp = System.currentTimeMillis(),
                            ruleId = rule.id,
                            ruleTitle = rule.title + suffix,
                            score = score,
                            isCustom = false
                        )
                        // [New] 추가 시 토스트 메시지
                        val msg = if (score >= 0) "+${score}점 추가됨" else "${score}점 차감됨"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        
                        tempRecords = tempRecords + newTempRecord
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { onSave(tempRecords) },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⚓ 항해 일지 최종 저장", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showAddCustomDialog) {
        AddRecordDialog(
            onDismiss = { showAddCustomDialog = false },
            onSave = { content, score ->
                val newCustomRecord = ScoreRecord(
                    date = date,
                    timestamp = System.currentTimeMillis(),
                    ruleId = null,
                    ruleTitle = content,
                    score = score,
                    isCustom = true
                )
                // [New] 직접 입력 시 토스트
                val msg = if (score >= 0) "+${score}점 추가됨" else "${score}점 차감됨"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                
                tempRecords = tempRecords + newCustomRecord
                showAddCustomDialog = false
            }
        )
    }
}

@Composable
fun LogRecordItem(record: ScoreRecord, onDelete: () -> Unit) {
    // Time formatting
    val timeStr = remember(record.timestamp) {
        val instant = java.time.Instant.ofEpochMilli(record.timestamp)
        val zoneId = java.time.ZoneId.systemDefault()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        instant.atZone(zoneId).format(formatter)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.ruleTitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${record.score} P", fontSize = 11.sp, color = if (record.score >= 0) Color(0xFF4CAF50) else Color.Red)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = timeStr, fontSize = 11.sp, color = Color.Gray)
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
        }
    }
}

@Composable
fun LogRuleItem(rule: Rule, onClick: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                Text(text = "+${rule.defaultScore} / ${rule.penalty}", fontSize = 10.sp, color = Color(0xFF5D4037))
            }
            Row {
                Button(
                    onClick = { onClick(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.height(26.dp).width(40.dp).padding(horizontal = 1.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("O", fontSize = 10.sp)
                }
                Button(
                    onClick = { onClick(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.height(26.dp).width(40.dp).padding(horizontal = 1.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("X", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var scoreStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoyageBackgroundParchment,
        title = { Text("직접 기록 추가", color = Color(0xFF3E2723)) },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = voyageTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = scoreStr,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '-' }) scoreStr = it },
                    label = { Text("점수 (예: 10, -5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = voyageTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val score = scoreStr.toIntOrNull() ?: 0
                    if (content.isNotBlank()) {
                        onSave(content, score)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Text("기록", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color(0xFF5D4037))
            }
        }
    )
}