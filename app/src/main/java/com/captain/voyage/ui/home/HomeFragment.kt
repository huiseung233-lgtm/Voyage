package com.captain.voyage.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // ★ 필수: MainActivity와 뷰모델 공유
import androidx.lifecycle.lifecycleScope
import com.captain.voyage.VoyageApplication
import com.captain.voyage.databinding.FragmentHomeBinding
import com.captain.voyage.ui.game.GameActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // MainActivity와 뷰모델 인스턴스를 공유하여 알림 신호를 주고받습니다.
    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory((requireActivity().application as VoyageApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 데이터 관찰 (날짜 및 점수)
        setupObservers()

        // 2. 클릭 이벤트 설정
        setupClickListeners()

        // 3. 알림 트리거 관찰 (자동 팝업 로직)
        // MainActivity에서 보낸 신호를 여기서 감지합니다.
        setupNotificationTrigger()
    }

    private fun setupObservers() {
        // 날짜 표시
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.displayDate.collect { dateStr ->
                binding.tvDate.text = dateStr
            }
        }

        // 오늘의 점수 표시
        viewModel.todayLog.observe(viewLifecycleOwner) { log ->
            val score = log?.totalScore ?: 0
            binding.tvScore.text = "$score P"
        }
    }

    private fun setupClickListeners() {
        // 상단 배너 클릭 -> 게임 화면 이동
        binding.cvDeckView.setOnClickListener {
            val intent = Intent(requireContext(), GameActivity::class.java)
            startActivity(intent)
        }

        // 날짜 카드 클릭 -> 캘린더 팝업
        binding.cvDate.setOnClickListener {
            CalendarDialog().show(parentFragmentManager, "CalendarDialog")
        }

        // 점수 카드 클릭 -> 로그북 팝업 (수동)
        binding.cvScore.setOnClickListener {
            openLogbookDialog()
        }
    }

    private fun setupNotificationTrigger() {
        // MainActivity가 triggerLogbookPopup()을 호출하면 이 옵저버가 실행됩니다.
        viewModel.navigateToLogbook.observe(viewLifecycleOwner) { shouldOpen ->
            if (shouldOpen == true) {
                openLogbookDialog()
                // 신호를 소모했음을 뷰모델에 알려 중복 팝업을 방지합니다.
                viewModel.onLogbookPopupOpened()
            }
        }
    }

    private fun openLogbookDialog() {
        val todayDate = viewModel.selectedDate.value ?: ""
        if (todayDate.isNotEmpty()) {
            LogbookDialog.newInstance(todayDate).show(parentFragmentManager, "LogbookDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}