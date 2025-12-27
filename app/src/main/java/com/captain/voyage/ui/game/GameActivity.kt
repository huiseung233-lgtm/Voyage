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

        // [New] 로그북 열기 신호 관찰
        viewModel.navigateToLogbook.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                val resultIntent = Intent().apply {
                    putExtra("OPEN_LOGBOOK_AND_DOCK", true) // 정박 연계 신호
                }
                setResult(RESULT_OK, resultIntent)
                finish() // 게임 화면 종료 -> 메인으로 복귀
            }
        }

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