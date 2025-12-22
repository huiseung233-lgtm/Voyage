package com.captain.voyage.ui.home

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.captain.voyage.ui.game.GameActivity
import com.captain.voyage.ui.theme.VoyageTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val displayDate by viewModel.displayDate.collectAsStateWithLifecycle()
    val todayLog by viewModel.todayLog.observeAsState()
    
    // Dialog States
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showLogbookDialog by remember { mutableStateOf(false) }
    
    // Navigation Signal from ViewModel (Notification Click)
    val navigateToLogbook by viewModel.navigateToLogbook.observeAsState()

    LaunchedEffect(navigateToLogbook) {
        if (navigateToLogbook == true) {
            showLogbookDialog = true
            viewModel.onLogbookPopupOpened()
        }
    }

    VoyageTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD7CCC8)) // Background color
                .padding(16.dp)
        ) {
            // 1. Deck View (Banner)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable {
                        context.startActivity(Intent(context, GameActivity::class.java))
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF81D4FA), Color(0xFF29B6F6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌊 View (Tap to Sail)",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dashboard Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Card -> Calendar
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF5F5DC),
                    onClick = { showCalendarDialog = true }
                ) {
                    Text(
                        text = displayDate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Text(
                        text = "Today",
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63)
                    )
                }

                // Score Card -> Logbook
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFFFF8E1),
                    onClick = {
                        // Open Logbook for Today
                        // (Assume selectedDate is already today in ViewModel init, or reset it here)
                        // But let's just show the dialog, which observes selectedDate
                        showLogbookDialog = true 
                    }
                ) {
                    val score = todayLog?.totalScore ?: 0
                    Text(
                        text = "$score P",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Text(
                        text = "Daily Score",
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63)
                    )
                }
            }
        }

        // Dialogs
        if (showCalendarDialog) {
            CalendarDialog(
                viewModel = viewModel,
                onDismiss = { showCalendarDialog = false },
                onDateSelected = { date ->
                    showCalendarDialog = false
                    // Update ViewModel selection and open Logbook
                    viewModel.setSelectedDate(date)
                    showLogbookDialog = true
                }
            )
        }

        if (showLogbookDialog) {
            // Pass the currently selected date from ViewModel
            val currentSelectedDate = viewModel.selectedDate.value ?: ""
            LogbookDialog(
                viewModel = viewModel,
                date = currentSelectedDate,
                onDismiss = { showLogbookDialog = false }
            )
        }
    }
}

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}
