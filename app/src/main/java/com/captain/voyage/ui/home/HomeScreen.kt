package com.captain.voyage.ui.home

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.captain.voyage.R
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.ui.game.GameActivity
import com.captain.voyage.ui.theme.VoyageWoodMedium
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val view = LocalView.current
    
    // Force System UI Colors
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as android.app.Activity).window
            val woodColor = VoyageWoodMedium.toArgb()
            window.statusBarColor = woodColor
            window.navigationBarColor = woodColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    val todayLog by viewModel.todayLog.observeAsState()
    val targetScore by viewModel.targetScore.collectAsStateWithLifecycle()
    val monthlyLogs by viewModel.monthlyLogs.collectAsStateWithLifecycle()
    
    // Calendar State
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var showLogbookDialog by remember { mutableStateOf(false) }
    
    // Navigation Signal from ViewModel
    val navigateToLogbook by viewModel.navigateToLogbook.observeAsState()

    // Scroll State
    val scrollState = rememberScrollState()

    // Animation State (Bobbing Effect)
    val infiniteTransition = rememberInfiniteTransition(label = "bobbing")
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f, 
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbingVal"
    )

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
            .background(Color(0xFF2d1e16)) // Root background
            .verticalScroll(scrollState)
    ) {
        // 1. Top Area: Sailing View (POV)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) 
                .background(Color.Black)
                .clickable { 
                    context.startActivity(Intent(context, GameActivity::class.java))
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_sea_horizon),
                contentDescription = "Sea Horizon",
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .offset(y = (-20).dp)
                    .offset { IntOffset(0, (bobbingOffset * 0.5f).roundToInt()) } 
            )

            Image(
                painter = painterResource(id = R.drawable.ui_window),
                contentDescription = "Window with Deck",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Bottom Area: Briefing Desk
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-10).dp)
                .background(Color(0xFF3E2723)),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_desk_new),
                contentDescription = "Desk Background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth() 
            )

            // Parchment Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 27.dp)
                    .padding(vertical = 24.dp, horizontal = 2.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ui_calendar_parchment),
                    contentDescription = "Logbook Parchment",
                    contentScale = ContentScale.FillBounds, // 비율에 상관없이 영역을 채우도록 변경
                    modifier = Modifier.matchParentSize() // 부모 Box의 최종 크기에 맞춤
                )

                // [1] Header & Progress Bar (Independent Layer - Floating Above)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-50).dp)
                        .padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Daily Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD7CCC8))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(12.dp).border(1.5.dp, Color(0xFFD7CCC8), RoundedCornerShape(6.dp)).clip(RoundedCornerShape(6.dp)).background(Color(0xFF3E2723))) {
                        LinearProgressIndicator(progress = { (todayLog?.totalScore?.toFloat() ?: 0f) / targetScore.toFloat() }, modifier = Modifier.fillMaxSize(), color = Color(0xFFFFD700), trackColor = Color.Transparent)
                    }
                }

                // [2] Calendar Content (Independent Layer - Inside Parchment)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 63.5.dp, end = 59.5.dp, top = 15.dp, bottom = 35.dp), // 하단 패딩 10dp 추가 확장
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Month Title with Navigation Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 0.dp) // 하단 패딩 제거 (그리드 4dp 상승)
                    ) {
                        Text(
                            text = "<",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E2723),
                            modifier = Modifier
                                .clickable { 
                                    currentYearMonth = currentYearMonth.minusMonths(1)
                                    viewModel.loadMonthlyLogs(currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                                }
                                .padding(horizontal = 12.dp)
                        )

                        Text(
                            text = "${currentYearMonth.year} . ${currentYearMonth.monthValue}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3E2723)
                        )

                        Text(
                            text = ">",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E2723),
                            modifier = Modifier
                                .clickable { 
                                    currentYearMonth = currentYearMonth.plusMonths(1)
                                    viewModel.loadMonthlyLogs(currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                                }
                                .padding(horizontal = 12.dp)
                        )
                    }

                    val calendarDays = remember(currentYearMonth, monthlyLogs) {
                        generateHomeCalendarDays(currentYearMonth, monthlyLogs)
                    }

                    val headerHeight = 24.dp
                    val cellHeight = 26.dp
                    val gridBorderColor = Color(0xFF3E2723).copy(alpha = 0.3f)
                    
                    val rowCount = (calendarDays.size + 6) / 7
                    val dynamicGridHeight = headerHeight + (cellHeight * rowCount)

                    // CALENDAR GRID
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .height(dynamicGridHeight)
                            .drawBehind {
                                val strokeWidth = 0.5.dp.toPx()
                                val canvasWidth = size.width
                                val startY = headerHeight.toPx()
                                val totalGridHeightPx = cellHeight.toPx() * rowCount
                                val endY = startY + totalGridHeightPx
                                val colWidth = canvasWidth / 7f
                                
                                // Vertical lines
                                for (i in 0..7) {
                                    val x = i * colWidth
                                    drawLine(
                                        color = gridBorderColor,
                                        start = Offset(x, startY),
                                        end = Offset(x, endY),
                                        strokeWidth = strokeWidth
                                    )
                                }
                                
                                // Horizontal lines
                                val rowHeightPx = cellHeight.toPx()
                                for (i in 0..rowCount) {
                                    val y = startY + (i * rowHeightPx)
                                    drawLine(
                                        color = gridBorderColor,
                                        start = Offset(0f, y),
                                        end = Offset(canvasWidth, y),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            },
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Headers
                        items(listOf("S", "M", "T", "W", "T", "F", "S")) { day ->
                            Box(
                                modifier = Modifier.height(headerHeight).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day == "S") Color(0xFFD32F2F) else Color(0xFF5D4037),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        // Days
                        items(calendarDays) { day ->
                            HomeCalendarDayItem(day) { selectedDate ->
                                val clickedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_DATE)
                                val today = LocalDate.now()
                                
                                if (clickedDate.isAfter(today)) {
                                    Toast.makeText(context, "No future records!", Toast.LENGTH_SHORT).show()
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
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showLogbookDialog) {
        val selectedDate = viewModel.selectedDate.value ?: ""
        LogbookDialog(
            viewModel = viewModel,
            date = selectedDate,
            onDismiss = { showLogbookDialog = false }
        )
    }
}

data class HomeCalendarDayUi(
    val date: String,
    val day: Int,
    val score: Int?,
    val isEmpty: Boolean,
    val isToday: Boolean = false 
)

@RequiresApi(Build.VERSION_CODES.O)
fun generateHomeCalendarDays(ym: YearMonth, logs: List<DailyLog>): List<HomeCalendarDayUi> {
    val daysInMonth = ym.lengthOfMonth()
    val firstDayOfMonth = ym.atDay(1)
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 
    val emptySlots = if (dayOfWeekValue == 7) 0 else dayOfWeekValue
    
    val today = LocalDate.now().toString()

    val list = mutableListOf<HomeCalendarDayUi>()
    for (i in 0 until emptySlots) {
        list.add(HomeCalendarDayUi("", 0, null, true))
    }
    for (day in 1..daysInMonth) {
        val dateStr = ym.atDay(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val log = logs.find { it.date == dateStr }
        val isToday = dateStr == today
        list.add(HomeCalendarDayUi(dateStr, day, log?.totalScore, false, isToday))
    }
    return list
}

@Composable
fun HomeCalendarDayItem(day: HomeCalendarDayUi, onClick: (String) -> Unit) {
    if (day.isEmpty) {
        Box(modifier = Modifier.size(26.dp))
    } else {
        val borderModifier = if (day.isToday) {
            Modifier.border(2.5.dp, Color(0xFFFFD700)) 
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .size(26.dp) 
                .then(borderModifier)
                .clickable { onClick(day.date) }
        ) {
            Text(
                text = "${day.day}", 
                fontSize = 10.sp,
                lineHeight = 10.sp,
                color = Color(0xFF3E2723),
                fontWeight = if(day.isToday) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 3.dp),
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )

            if (day.score != null) {
                val score = day.score
                val scoreColor = when {
                    score >= 80 -> Color(0xFF388E3C) 
                    score >= 50 -> Color(0xFFFF8F00) 
                    else -> Color(0xFFD32F2F) 
                }
                Text(
                    text = "$score",
                    fontSize = 8.sp,
                    lineHeight = 8.sp,
                    color = scoreColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
    }
}