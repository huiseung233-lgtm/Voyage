package com.captain.voyage.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.captain.voyage.VoyageApplication
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.databinding.DialogCalendarBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarDialog : DialogFragment() {

    private var _binding: DialogCalendarBinding? = null
    private val binding get() = _binding!!

    // HomeViewModel 공유 (Activity 범위)
    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory((requireActivity().application as VoyageApplication).repository)
    }

    private lateinit var calendarAdapter: CalendarAdapter
    private var currentYearMonth: YearMonth = YearMonth.now() // 현재 보고 있는 달력의 연/월

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCalendarBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // 팝업 크기 조절 (가로 꽉 차게)
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupObservers()

        // 처음 켜지면 현재 월 데이터 요청
        loadCalendarData(currentYearMonth)
    }

    private fun setupRecyclerView() {
        // 날짜 클릭 시 동작: 캘린더 닫고 -> 로그북 열기
        calendarAdapter = CalendarAdapter { selectedDate ->
            dismiss() // 나(달력)는 퇴장
            LogbookDialog.newInstance(selectedDate).show(parentFragmentManager, "LogbookDialog") // 로그북 입장
        }

        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(context, 7) // 7열 (일~토)
            adapter = calendarAdapter
        }
    }

    private fun setupListeners() {
        binding.btnCloseCalendar.setOnClickListener { dismiss() }

        // 이전 달 버튼
        binding.btnPrevMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            loadCalendarData(currentYearMonth)
        }

        // 다음 달 버튼
        binding.btnNextMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            loadCalendarData(currentYearMonth)
        }
    }

    private fun setupObservers() {
        // DB에서 월간 점수 데이터가 오면 달력을 다시 그림
        viewModel.monthlyLogs.observe(viewLifecycleOwner) { logs ->
            updateCalendarUI(logs)
        }
    }

    private fun loadCalendarData(ym: YearMonth) {
        // "2025-12" 형식으로 변환
        val yearMonthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"))

        // UI 타이틀 변경
        binding.tvMonthTitle.text = "${ym.year}년 ${ym.monthValue}월"

        // 뷰모델에게 데이터 가져오라고 시킴
        viewModel.loadMonthlyLogs(yearMonthStr)
    }

    // ★ 달력의 핵심 로직 (빈 칸 계산 + 날짜 채우기 + 점수 매칭)
    private fun updateCalendarUI(logs: List<DailyLog>) {
        val daysInMonth = currentYearMonth.lengthOfMonth() // 이번 달이 며칠까지 있는지 (28/30/31)
        val firstDayOfMonth = currentYearMonth.atDay(1) // 1일

        // 1일이 무슨 요일인지? (월=1 ... 일=7)
        // 우리 달력은 '일요일'부터 시작하므로 계산 필요
        // 일(7) -> 0칸 띔, 월(1) -> 1칸 띔 ... 토(6) -> 6칸 띔
        val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value
        val emptySlots = if (dayOfWeekValue == 7) 0 else dayOfWeekValue

        val calendarList = mutableListOf<CalendarDay>()

        // 1. 앞쪽 빈 칸 채우기
        for (i in 0 until emptySlots) {
            calendarList.add(CalendarDay("", 0, null, true))
        }

        // 2. 1일부터 말일까지 채우기
        for (day in 1..daysInMonth) {
            val dateStr = currentYearMonth.atDay(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // 이 날짜에 해당하는 로그가 있는지 찾기
            val log = logs.find { it.date == dateStr }

            calendarList.add(
                CalendarDay(
                    date = dateStr,
                    day = day,
                    score = log?.totalScore, // 점수가 있으면 넣고 없으면 null
                    isEmpty = false
                )
            )
        }

        calendarAdapter.submitList(calendarList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}