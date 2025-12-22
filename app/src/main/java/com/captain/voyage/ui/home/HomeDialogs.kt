package com.captain.voyage.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // Added
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog // Added
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState // Added
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header (Month Navigation)
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

                // Days Grid
                val calendarDays = remember(currentYearMonth, monthlyLogs) {
                    generateCalendarDays(currentYearMonth, monthlyLogs)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(300.dp)
                ) {
                    // Weekday Headers
                    items(listOf("일", "월", "화", "수", "목", "금", "토")) { day ->
                        Text(
                            text = day,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }
                    // Days
                    items(calendarDays) { day ->
                        CalendarDayItem(day, onDateSelected)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                ) {
                    Text("닫기", color = Color.White)
                }
            }
        }
    }
}

data class CalendarDayUi(
    val date: String,
    val day: Int,
    val score: Int?,
    val isEmpty: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
fun generateCalendarDays(ym: YearMonth, logs: List<DailyLog>): List<CalendarDayUi> {
    val daysInMonth = ym.lengthOfMonth()
    val firstDayOfMonth = ym.atDay(1)
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value
    val emptySlots = if (dayOfWeekValue == 7) 0 else dayOfWeekValue

    val list = mutableListOf<CalendarDayUi>()
    for (i in 0 until emptySlots) {
        list.add(CalendarDayUi("", 0, null, true))
    }
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
            (day.score ?: 0) >= 80 -> Color(0xFF4CAF50) // Green
            (day.score ?: 0) >= 50 -> Color(0xFFFF9800) // Orange
            else -> Color(0xFF9E9E9E) // Grey
        }

        Column(
            modifier = Modifier
                .padding(2.dp)
                .clickable { onClick(day.date) }
                .background(Color.White, RoundedCornerShape(4.dp))
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "${day.day}", fontSize = 12.sp, color = Color.Black)
            if (hasScore) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(scoreColor, CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}


// --- Logbook Dialog ---

@Composable
fun LogbookDialog(
    viewModel: HomeViewModel,
    date: String,
    onDismiss: () -> Unit
) {
    val records by viewModel.selectedDateRecords.observeAsState(emptyList())
    val rules by viewModel.allRules.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        viewModel.setSelectedDate(date)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(700.dp) // Fixed height for consistency
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 $date 항해 일지",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                // Total Score of Day (Calculate from records to be accurate)
                val dayScore = records.sumOf { it.score }
                Text(
                    text = "일일 합계 : ${if (dayScore > 0) "+" else ""}$dayScore P",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                HorizontalDivider(color = Color(0xFF8D6E63))

                // History List (Top Half)
                Text("기록 내역", modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = Color.Gray)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    items(records) { record ->
                        LogRecordItem(record) { viewModel.deleteRecord(record) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF8D6E63))

                // Rules List (Bottom Half)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("규칙 적용", fontSize = 12.sp, color = Color.Gray)
                    Button(
                        onClick = { showAddCustomDialog = true },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) {
                        Text("직접 입력", fontSize = 12.sp)
                    }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("규칙 검색...") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    trailingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    val filteredRules = if (searchQuery.isBlank()) rules else rules.filter {
                        it.title.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredRules) { rule ->
                        LogRuleItem(rule) { isSuccess ->
                            val score = if (isSuccess) rule.defaultScore else rule.penalty
                            val suffix = if (isSuccess) "" else " (실패)"
                            // [수정] rule.id 전달
                            viewModel.addRecord(date, rule.title + suffix, score, ruleId = rule.id)
                        }
                    }
                }
            }
        }
    }

    if (showAddCustomDialog) {
        AddRecordDialog(
            onDismiss = { showAddCustomDialog = false },
            onSave = { content, score ->
                viewModel.addRecord(date, content, score)
                showAddCustomDialog = false
            }
        )
    }
}

@Composable
fun LogRecordItem(record: ScoreRecord, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.ruleTitle, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(text = "${record.score} P", fontSize = 12.sp, color = if (record.score >= 0) Color(0xFF4CAF50) else Color.Red)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
        }
    }
}

@Composable
fun LogRuleItem(rule: Rule, onClick: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                Text(text = "+${rule.defaultScore} / ${rule.penalty}", fontSize = 12.sp, color = Color(0xFF5D4037))
            }
            Row {
                Button(
                    onClick = { onClick(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.height(30.dp).width(50.dp).padding(horizontal = 2.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("O", fontSize = 12.sp)
                }
                Button(
                    onClick = { onClick(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.height(30.dp).width(50.dp).padding(horizontal = 2.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("X", fontSize = 12.sp)
                }
            }
        }
    }
}

// --- Add Record Dialog ---

@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var scoreStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("직접 기록 추가") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = scoreStr,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '-' }) scoreStr = it },
                    label = { Text("점수 (예: 10, -5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
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
                }
            ) {
                Text("기록")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
