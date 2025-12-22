package com.captain.voyage.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.captain.voyage.ui.theme.VoyageTheme

@Composable
fun GoalsScreen() {
    VoyageTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFADD8E6)), // Light Blue from XML
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "여기는 항로(Goals) 탭입니다",
                color = Color.Black,
                fontSize = 20.sp
            )
        }
    }
}
