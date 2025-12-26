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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.captain.voyage.R
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.ui.game.GameActivity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.captain.voyage.ui.theme.VoyageWoodMedium

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
        // =====================================================================
        // 1. Top Area: Sailing View (POV)
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) 
                .background(Color.Black)
                .clickable { 
                    context.startActivity(Intent(context, GameActivity::class.java))
                }
        ) {
            // 1. Sea & Sky
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

            // 2. Window Frame with Deck (Restored)
            Image(
                painter = painterResource(id = R.drawable.ui_window),
                contentDescription = "Window with Deck",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // =====================================================================
        // 2. Bottom Area: Briefing Desk
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-10).dp) // Updated to match VoyageWoodMedium
                .background(Color(0xFF3E2723)),
            contentAlignment = Alignment.TopCenter
        ) {
            // (0) Desk Background Texture
            Image(
                painter = painterResource(id = R.drawable.bg_desk_new),
                contentDescription = "Desk Background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth() 
            )

            // (1) Parchment Container

                                    Box(

                                        modifier = Modifier

                                            .fillMaxWidth()

                                            .offset(y = 27.dp)

                                            .padding(vertical = 24.dp, horizontal = 2.dp), // Removed shadow

                                        contentAlignment = Alignment.TopCenter

                                    ) {

                                        // ... (Parchment Background and Content Area Header remain same)

                                        // (Simplified context for matching)

                                        Image(

                                            painter = painterResource(id = R.drawable.ui_calendar_parchment),

                                            contentDescription = "Logbook Parchment",

                                            contentScale = ContentScale.FillWidth,

                                            modifier = Modifier.fillMaxWidth()

                                        )

                        

                                        Column(

                                            modifier = Modifier

                                                .fillMaxWidth()

                                                .offset(y = (-40).dp) 

                                                .padding(start = 42.dp, end = 38.dp, top = 10.dp, bottom = 40.dp), 

                                            horizontalAlignment = Alignment.CenterHorizontally

                                        ) {

                                            // Header & Progress Bar

                                            Column(

                                                modifier = Modifier.offset(y = (-7).dp),

                                                horizontalAlignment = Alignment.CenterHorizontally

                                            ) {

                                                Text(text = "Daily Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Box(modifier = Modifier.fillMaxWidth().height(12.dp).border(1.5.dp, Color(0xFF3E2723), RoundedCornerShape(6.dp)).clip(RoundedCornerShape(6.dp)).background(Color(0xFFD7CCC8))) {

                                                    LinearProgressIndicator(progress = { (todayLog?.totalScore?.toFloat() ?: 0f) / targetScore.toFloat() }, modifier = Modifier.fillMaxSize(), color = Color(0xFFFFD700), trackColor = Color.Transparent)

                                                }

                                            }

                        

                                            Spacer(modifier = Modifier.height(4.dp)) 

                        

                                            // Month Title - Remains at 15dp offset

                                            Text(

                                                text = "${currentYearMonth.year} . ${currentYearMonth.monthValue}",

                                                fontSize = 16.sp,

                                                fontWeight = FontWeight.ExtraBold,

                                                color = Color(0xFF3E2723),

                                                modifier = Modifier.offset(y = 5.dp).padding(bottom = 4.dp)

                                            )

                        

                                            val calendarDays = remember(currentYearMonth, monthlyLogs) {

                                                generateHomeCalendarDays(currentYearMonth, monthlyLogs)

                                            }

                        

                                            // SUPER COMPACT CALENDAR GRID - Moved down by 4dp further (from 15 to 19)

                                            LazyVerticalGrid(

                                                columns = GridCells.Fixed(7),

                                                modifier = Modifier

                                                    .height(180.dp)

                                                    .offset(y = 9.dp), // Increased spacing by 4dp

                                                userScrollEnabled = false

                                            ) {
                        // Headers
                        items(listOf("S", "M", "T", "W", "T", "F", "S")) { day ->
                            Text(
                                text = day,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (day == "S") Color(0xFFD32F2F) else Color(0xFF5D4037),
                                fontSize = 8.sp
                            )
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
        Box(modifier = Modifier.size(28.dp))
    } else {
        val score = day.score
        
        // Stamp Color Logic
        val stampColor = when {
            score == null -> Color.Transparent
            score >= 80 -> Color(0xFF388E3C) 
            score >= 50 -> Color(0xFFFF8F00) 
            else -> Color(0xFFD32F2F) 
        }

        // Today Highlight
        val borderModifier = if (day.isToday) {
            Modifier.border(1.dp, Color(0xFFFFD700), CircleShape)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .size(28.dp) 
                .then(borderModifier)
                .clip(CircleShape)
                .clickable { onClick(day.date) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${day.day}", 
                    fontSize = 9.sp, 
                    color = Color(0xFF3E2723),
                    fontWeight = if(day.isToday) FontWeight.Bold else FontWeight.Normal
                )
                if (score != null) {
                     Box(
                         modifier = Modifier
                             .size(4.dp) 
                             .background(stampColor, CircleShape)
                     )
                }
            }
        }
    }
}