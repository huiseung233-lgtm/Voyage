package com.captain.voyage.ui.home

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border // Added
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.ui.game.GameActivity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val todayLog by viewModel.todayLog.observeAsState()
    val targetScore by viewModel.targetScore.collectAsStateWithLifecycle()
    val monthlyLogs by viewModel.monthlyLogs.observeAsState(emptyList())
    
    // Calendar State
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var showLogbookDialog by remember { mutableStateOf(false) }
    
    // Navigation Signal from ViewModel
    val navigateToLogbook by viewModel.navigateToLogbook.observeAsState()

    // Scroll State
    val scrollState = rememberScrollState()

    // Init Data
    LaunchedEffect(Unit) {
        viewModel.loadMonthlyLogs(currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
    }

    LaunchedEffect(navigateToLogbook) {
        if (navigateToLogbook == true) {
            viewModel.setSelectedDate(java.time.LocalDate.now().toString())
            showLogbookDialog = true
            viewModel.onLogbookPopupOpened()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7CCC8)) // 기본 배경색
            .verticalScroll(scrollState)
    ) {
        // 1. Top Image Area (Ship/Sea)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D47A1), // 깊은 바다색
                            Color(0xFF1976D2)  // 얕은 바다색
                        )
                    )
                )
                .clickable { 
                    context.startActivity(Intent(context, GameActivity::class.java))
                }
        ) {
            // 배 아이콘
            Text(
                text = "🚢",
                fontSize = 100.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            // 안내 텍스트
            Text(
                text = "Tap to Sail 🌊",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Calendar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // (1) Header
                Text(
                    text = "Daily Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // (2) Gauge Bar
                val currentScore = todayLog?.totalScore ?: 0
                val progress = (currentScore.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)
                
                Box(contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFFE0E0E0),
                    )
                    Text(
                        text = "$currentScore / $targetScore P",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // (3) Calendar Grid
                Text(
                    text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val calendarDays = remember(currentYearMonth, monthlyLogs) {
                    generateHomeCalendarDays(currentYearMonth, monthlyLogs)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(350.dp),
                    userScrollEnabled = false
                ) {
                    // Headers
                    items(listOf("일", "월", "화", "수", "목", "금", "토")) { day ->
                        Text(
                            text = day,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (day == "일") Color.Red else Color(0xFF5D4037),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // Days
                    items(calendarDays) { day ->
                        HomeCalendarDayItem(day) { selectedDate ->
                            // [수정] 미래 날짜 클릭 방지
                            val clickedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_DATE)
                            val today = LocalDate.now()
                            
                            if (clickedDate.isAfter(today)) {
                                Toast.makeText(context, "미래의 기록은 작성할 수 없습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.setSelectedDate(selectedDate)
                                showLogbookDialog = true
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showLogbookDialog) {
        val selectedDate = viewModel.selectedDate.value ?: ""
        LogbookDialog(
            viewModel = viewModel,
            date = selectedDate,
            onDismiss = { showLogbookDialog = false }
        )
    }
}

// --- Helper Data & Components ---

data class HomeCalendarDayUi(
    val date: String,
    val day: Int,
    val score: Int?,
    val isEmpty: Boolean,
    val isToday: Boolean = false // Added
)

@RequiresApi(Build.VERSION_CODES.O)
fun generateHomeCalendarDays(ym: YearMonth, logs: List<DailyLog>): List<HomeCalendarDayUi> {
    val daysInMonth = ym.lengthOfMonth()
    val firstDayOfMonth = ym.atDay(1)
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value
    val emptySlots = dayOfWeekValue % 7
    val today = LocalDate.now().toString() // 오늘 날짜

    val list = mutableListOf<HomeCalendarDayUi>()
    for (i in 0 until emptySlots) {
        list.add(HomeCalendarDayUi("", 0, null, true))
    }
    for (day in 1..daysInMonth) {
        val dateStr = ym.atDay(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val log = logs.find { it.date == dateStr }
        // 오늘인지 확인
        val isToday = dateStr == today
        list.add(HomeCalendarDayUi(dateStr, day, log?.totalScore, false, isToday))
    }
    return list
}

@Composable
fun HomeCalendarDayItem(day: HomeCalendarDayUi, onClick: (String) -> Unit) {
    if (day.isEmpty) {
        Box(modifier = Modifier.size(40.dp))
    } else {
        val score = day.score
        val hasScore = score != null
        
        val badgeColor = when {
            score == null -> Color.Transparent
            score >= 80 -> Color(0xFF4CAF50)
            score >= 50 -> Color(0xFFFF9800)
            else -> Color(0xFFE53935)
        }

        // 오늘 날짜 강조 스타일
        val modifier = if (day.isToday) {
            Modifier
                .padding(2.dp)
                .size(48.dp)
                .background(Color(0xFFFFF9C4), CircleShape) // 연한 노란 배경
                .border(2.dp, Color(0xFFFFD700), CircleShape) // 금색 테두리
                .clip(CircleShape)
                .clickable { onClick(day.date) }
        } else {
            Modifier
                .padding(2.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onClick(day.date) }
        }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 날짜
                Text(
                    text = "${day.day}", 
                    fontSize = 14.sp, 
                    color = Color(0xFF3E2723),
                    fontWeight = FontWeight.Medium
                )
                // 점수 (Added)
                if (hasScore) {
                    Text(
                        text = "${score ?: 0}",
                        fontSize = 10.sp,
                        color = badgeColor, // 점수 구간별 색상 적용
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}