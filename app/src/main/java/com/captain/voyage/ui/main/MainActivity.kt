package com.captain.voyage.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint

import com.captain.voyage.ui.theme.VoyageTheme

import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.captain.voyage.ui.theme.VoyageTextPrimary

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Enable Edge-to-Edge (System bar transparent)
        enableEdgeToEdge()

        setContent {
            VoyageTheme {
                // 2. Root Container with "Status Bar Color"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoyageTextPrimary) // This color shows behind the transparent status bar
                ) {
                    // 3. App Content with Padding (Pushes content below status bar)
                    Box(modifier = Modifier.safeDrawingPadding()) {
                         VoyageApp()
                    }
                }
            }
        }
    }
}