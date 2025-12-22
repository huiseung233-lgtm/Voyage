package com.captain.voyage.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.databinding.ItemLogHistoryBinding
import com.captain.voyage.databinding.ItemLogRuleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. 상단 '기록 내역' 어댑터
class HistoryAdapter(
    private val onDeleteClick: (ScoreRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var items: List<ScoreRecord> = emptyList()

    fun submitList(newItems: List<ScoreRecord>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemLogHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class HistoryViewHolder(private val binding: ItemLogHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ScoreRecord) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.tvTime.text = timeFormat.format(Date(record.timestamp))
            binding.tvTitle.text = record.ruleTitle

            // ★ [수정됨] 양수/음수 구분 및 색상 처리
            if (record.score > 0) {
                // 양수일 때: 앞에 + 붙이고 초록색
                binding.tvScore.text = "+${record.score} P"
                binding.tvScore.setTextColor(Color.parseColor("#2E7D32"))
            } else {
                // 음수(또는 0)일 때: 그냥 숫자만 보여주고(마이너스는 숫자에 포함됨) 빨간색
                binding.tvScore.text = "${record.score} P"
                binding.tvScore.setTextColor(Color.parseColor("#C62828"))
            }

            binding.btnDelete.setOnClickListener { onDeleteClick(record) }
        }
    }
}

// 2. 하단 '규칙 수행' 어댑터
class RuleActionAdapter(
    private val onActionClick: (Rule, Boolean) -> Unit
) : RecyclerView.Adapter<RuleActionAdapter.RuleViewHolder>() {

    private var items: List<Rule> = emptyList()

    fun submitList(newItems: List<Rule>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemLogRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class RuleViewHolder(private val binding: ItemLogRuleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: Rule) {
            binding.tvRuleTitle.text = rule.title

            // 성공 버튼 텍스트 (기존 defaultScore 유지)
            binding.btnSuccess.text = "✅ 성공 (+${rule.defaultScore})"

            // 2. ★ [수정] 실패 버튼: penalty 사용 (xml에 하드코딩된 '0' 대신 진짜 값 표시)
            // penalty가 보통 음수(-10)니까 괄호 안에 그대로 넣으면 "(-10)"이 됩니다.
            binding.btnFail.text = "🔴 실패 (${rule.penalty})"

            binding.btnSuccess.setOnClickListener { onActionClick(rule, true) }
            binding.btnFail.setOnClickListener { onActionClick(rule, false) }
        }
    }
}