package com.captain.voyage.ui.rules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.captain.voyage.data.model.Rule
import com.captain.voyage.databinding.ItemRuleBinding

class RuleAdapter(
    private val onItemClick: (Rule) -> Unit,
    private val onDeleteClick: (Rule) -> Unit
) : ListAdapter<Rule, RuleAdapter.RuleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RuleViewHolder(val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: Rule) {
            binding.tvRuleTitle.text = rule.title

            // 설명이 비어있으면 숨김 처리
            if (rule.description.isNotBlank()) {
                binding.tvRuleDesc.text = rule.description
                binding.tvRuleDesc.visibility = android.view.View.VISIBLE
            } else {
                binding.tvRuleDesc.visibility = android.view.View.GONE
            }

            // 점수 표시: +10 P / -10 P
            binding.tvRuleScore.text = "성공 +${rule.defaultScore} P  |  실패 ${rule.penalty} P"

            binding.root.setOnClickListener { onItemClick(rule) }
            binding.btnDelete.setOnClickListener { onDeleteClick(rule) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Rule>() {
            override fun areItemsTheSame(oldItem: Rule, newItem: Rule) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Rule, newItem: Rule) = oldItem == newItem
        }
    }
}