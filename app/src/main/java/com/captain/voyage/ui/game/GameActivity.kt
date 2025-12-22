package com.captain.voyage.ui.game

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.captain.voyage.VoyageApplication
import com.captain.voyage.data.model.PenaltyType
import com.captain.voyage.data.model.ShipStatus
import com.captain.voyage.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    // 뷰모델 연결
    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory((application as VoyageApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullScreen() // 1. 전체 화면 설정
        setupObservers()  // 2. 데이터 관찰 (UI 업데이트)
        setupListeners()  // 3. 클릭 이벤트
    }

    // 1. 몰입형 전체 화면 만들기 (상단바 숨김)
    private fun setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // 2. 데이터 관찰 및 UI 갱신
    private fun setupObservers() {
        // (1) 배 상태 관찰 (항해 vs 정박)
        viewModel.ship.observe(this) { ship ->
            if (ship == null) return@observe

            if (ship.status == ShipStatus.SAILING) {
                // 🌊 항해 모드 UI
                binding.btnAction.text = "⚓ 정박하기 (하루 마감)"
                binding.btnAction.setBackgroundColor(Color.parseColor("#1565C0")) // 파란색
                binding.ivBackground.setBackgroundColor(Color.parseColor("#29B6F6")) // 임시 바다 배경색

                // 상점 숨기기
                binding.viewShopTouchArea.visibility = View.GONE
                binding.tvShopLabel.visibility = View.GONE
            } else {
                // ⚓ 정박 모드 UI
                binding.btnAction.text = "⛵ 출항하기"
                binding.btnAction.setBackgroundColor(Color.parseColor("#4E342E")) // 갈색
                binding.ivBackground.setBackgroundColor(Color.parseColor("#1A237E")) // 임시 밤 항구 배경색

                // 상점 보이기 (투명 버튼 활성화)
                binding.viewShopTouchArea.visibility = View.VISIBLE
                binding.tvShopLabel.visibility = View.VISIBLE
            }
        }

        // (2) 유저 상태 관찰 (골드 & 피로도)
        viewModel.userStatus.observe(this) { status ->
            if (status == null) return@observe

            // 골드 표시
            binding.tvGold.text = "${status.gold} G"

            // 상태 아이콘 & 텍스트 변경
            when (status.penaltyType) {
                PenaltyType.NONE -> {
                    binding.tvStatusIcon.text = "⛵"
                    binding.tvStatusText.text = "순항 중"
                    binding.tvStatusText.setTextColor(Color.parseColor("#1B5E20")) // 녹색
                }
                PenaltyType.FATIGUE -> {
                    binding.tvStatusIcon.text = "😫"
                    binding.tvStatusText.text = "피로함"
                    binding.tvStatusText.setTextColor(Color.parseColor("#F57F17")) // 주황색
                }
                PenaltyType.LAZINESS -> {
                    binding.tvStatusIcon.text = "💤"
                    binding.tvStatusText.text = "나태함"
                    binding.tvStatusText.setTextColor(Color.parseColor("#5D4037")) // 갈색
                }
                PenaltyType.DOOM -> {
                    binding.tvStatusIcon.text = "☠️"
                    binding.tvStatusText.text = "위험!"
                    binding.tvStatusText.setTextColor(Color.parseColor("#B71C1C")) // 빨간색
                }
            }
        }

        // ★ [New] (3) 토스트 메시지 관찰 (이 부분이 추가되어야 반응함!)
        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 3. 버튼 클릭 이벤트
    private fun setupListeners() {
        // 나가기 (X)
        binding.btnExit.setOnClickListener {
            finish() // 액티비티 종료 -> 홈으로 복귀
        }

        // 메인 버튼 (정박/출항)
        binding.btnAction.setOnClickListener {
            viewModel.toggleShipStatus()
        }

        // 상점 (투명 버튼) 클릭
        binding.viewShopTouchArea.setOnClickListener {
            Toast.makeText(this, "🏪 상점 주인: '아직 오픈 준비 중일세!'", Toast.LENGTH_SHORT).show()
            // 나중에 여기에 ShopDialog.show() 연결하면 됨
        }
    }
}