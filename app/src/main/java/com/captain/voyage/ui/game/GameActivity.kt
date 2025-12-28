package com.captain.voyage.ui.game

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint

import android.content.Intent // Import 추가

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    // 뷰모델 연결 (Hilt가 주입)
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFullScreen() // 1. 전체 화면 설정

        setContent {
            GameScreen(
                viewModel = viewModel,
                onExit = { finish() }
            )
        }
    }

    // 1. 몰입형 전체 화면 만들기 (상단바 숨김)
    private fun setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}