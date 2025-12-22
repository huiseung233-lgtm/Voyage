package com.captain.voyage.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.captain.voyage.VoyageApplication
import com.captain.voyage.data.model.Rule
import com.captain.voyage.databinding.DialogLogbookBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogbookDialog : DialogFragment() {

    private var _binding: DialogLogbookBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory((requireActivity().application as VoyageApplication).repository)
    }

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var ruleAdapter: RuleActionAdapter

    // 이번 팝업 세션용 합계 점수
    private var sessionScore = 0

    // 검색용 원본 리스트
    private var fullRuleList: List<Rule> = emptyList()

    // ★ [New] 이 다이얼로그가 처리할 날짜 (기본값: 오늘)
    private var targetDate: String = ""

    // ★ [New] 외부에서 날짜를 받아 생성하는 공장(Factory) 메서드
    companion object {
        private const val ARG_DATE = "arg_date"

        fun newInstance(date: String): LogbookDialog {
            val fragment = LogbookDialog()
            val args = Bundle()
            args.putString(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 인자로 받은 날짜 꺼내기. 없으면 오늘 날짜.
        targetDate = arguments?.getString(ARG_DATE)
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // ★ ViewModel에게 "나 이제 이 날짜 볼거야"라고 알려줌
        viewModel.setSelectedDate(targetDate)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogLogbookBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            (resources.displayMetrics.heightPixels * 0.90).toInt()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 타이틀에 날짜 표시
        binding.tvHeaderDate.text = "📜 $targetDate 항해 일지"

        updateSessionScoreUI()
        setupRecyclerViews()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerViews() {
        // 1. 기록 리스트
        historyAdapter = HistoryAdapter { record ->
            viewModel.deleteRecord(record)
            sessionScore -= record.score
            updateSessionScoreUI()
            Toast.makeText(context, "기록 삭제됨", Toast.LENGTH_SHORT).show()
        }
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }

        // 2. 규칙 리스트
        ruleAdapter = RuleActionAdapter { rule, isSuccess ->
            val scoreToApply = if (isSuccess) rule.defaultScore else rule.penalty
            val titleSuffix = if (isSuccess) "" else " (실패)"

            // ★ [수정] targetDate를 사용하여 기록 추가 (과거 날짜 지원)
            viewModel.addRecord(targetDate, rule.title + titleSuffix, scoreToApply)

            sessionScore += scoreToApply
            updateSessionScoreUI()

            val msg = if(isSuccess) "${rule.title} 완료!" else "패널티 적용됨 (${rule.penalty})"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
        binding.rvRules.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ruleAdapter
        }
    }

    private fun setupObservers() {
        // ★ [수정] selectedDateRecords 관찰 (선택된 날짜의 기록)
        viewModel.selectedDateRecords.observe(viewLifecycleOwner) { records ->
            historyAdapter.submitList(records)
            // 기록이 바뀌면 세션 점수가 아니라 '실제 합계'를 보여주는게 더 정확할 수 있지만,
            // 일단 UI 가이드에 따라 세션 합계 로직을 유지하거나, 아래처럼 일일 합계로 바꿀 수도 있습니다.
            // 여기서는 기존 로직 유지를 위해 historyAdapter 갱신만 합니다.
        }

        // 규칙 리스트 관찰
        viewModel.allRules.observe(viewLifecycleOwner) { rules ->
            fullRuleList = rules
            filterRules(binding.etSearch.text.toString())
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSubmit.setOnClickListener { dismiss() }

        binding.btnCustomInput.setOnClickListener {
            AddRecordDialog { content, score ->
                // ★ [수정] targetDate 사용
                viewModel.addRecord(targetDate, content, score)
                sessionScore += score
                updateSessionScoreUI()
            }.show(parentFragmentManager, "CustomInput")
        }

        // 검색창 리스너
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRules(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterRules(query: String) {
        val filteredList = if (query.isBlank()) {
            fullRuleList
        } else {
            fullRuleList.filter { rule ->
                rule.title.contains(query, ignoreCase = true)
            }
        }
        ruleAdapter.submitList(filteredList)
    }

    private fun updateSessionScoreUI() {
        binding.tvTotalScore.text = "🧾 이번 결재 합계 : ${if (sessionScore > 0) "+" else ""}$sessionScore P"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}