package com.captain.voyage.ui.rules

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.captain.voyage.R
import com.captain.voyage.VoyageApplication
import com.captain.voyage.data.model.Rule
import com.captain.voyage.databinding.FragmentRulesBinding
import kotlinx.coroutines.launch
import java.util.Collections

class RulesFragment : Fragment() {

    private var _binding: FragmentRulesBinding? = null
    private val binding get() = _binding!!

    // ViewModel 연결
    private val viewModel: RulesViewModel by viewModels {
        RulesViewModelFactory((requireActivity().application as VoyageApplication).repository)
    }

    private lateinit var adapter: RuleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupSearch()

        // 4. 추가 버튼 클릭 -> (null 전달 = 추가 모드)
        binding.fabAddRule.setOnClickListener {
            showRuleDialog(null)
        }
    }

    private fun setupAdapter() {
        adapter = RuleAdapter(
            onItemClick = { rule ->
                // ★ [수정됨] 규칙 클릭 시 -> (rule 전달 = 수정 모드)
                showRuleDialog(rule)
            },
            onDeleteClick = { rule ->
                showDeleteConfirmDialog(rule)
            }
        )
        binding.rvRules.layoutManager = LinearLayoutManager(context)
        binding.rvRules.adapter = adapter

        // 데이터 관찰 (DB -> UI)
        lifecycleScope.launch {
            viewModel.rules.collect { rules ->
                adapter.submitList(rules)
            }
        }

        // 드래그 앤 드롭 설정 (기존 유지)
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            private var currentDragList: MutableList<Rule>? = null

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    currentDragList = adapter.currentList.toMutableList()
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                currentDragList?.let { list ->
                    if (fromPos < list.size && toPos < list.size) {
                        Collections.swap(list, fromPos, toPos)
                        adapter.notifyItemMoved(fromPos, toPos)
                    }
                }
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                currentDragList?.let { list ->
                    viewModel.updateRulesOrder(list)
                }
                currentDragList = null
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.rvRules)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ★ [업그레이드] 추가/수정 공용 다이얼로그
    // rule이 null이면 추가 모드, 있으면 수정 모드
    private fun showRuleDialog(rule: Rule?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_rule, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 뷰 찾기
        // 주의: dialog_add_rule.xml에 id가 'tv_dialog_title'인 TextView가 있어야 합니다.
        val tvHeader = dialogView.findViewById<TextView>(R.id.tv_dialog_title)

        val etTitle = dialogView.findViewById<EditText>(R.id.et_title)
        val etDesc = dialogView.findViewById<EditText>(R.id.et_desc)
        val etReward = dialogView.findViewById<EditText>(R.id.et_reward)
        val etPenalty = dialogView.findViewById<EditText>(R.id.et_penalty)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        // ★ 데이터 채우기 (수정 모드일 경우)
        if (rule != null) {
            // 수정 모드 제목 설정
            tvHeader?.text = "📜 규율 내용 수정"
            etTitle.setText(rule.title)
            etDesc.setText(rule.description)
            etReward.setText(rule.defaultScore.toString())
            etPenalty.setText(rule.penalty.toString())
            btnSave.text = "수정 완료"
        } else {
            // 추가 모드 제목 설정
            tvHeader?.text = "📜 새로운 선박 규율"
            btnSave.text = "서명 ✍️"
        }

        // 버튼 로직 (Stepper) - 보상
        dialogView.findViewById<Button>(R.id.btn_reward_plus).setOnClickListener {
            val current = etReward.text.toString().toIntOrNull() ?: 0
            etReward.setText("${current + 5}")
        }
        dialogView.findViewById<Button>(R.id.btn_reward_minus).setOnClickListener {
            val current = etReward.text.toString().toIntOrNull() ?: 0
            etReward.setText("${current - 5}")
        }

        // 버튼 로직 (Stepper) - 패널티
        dialogView.findViewById<Button>(R.id.btn_penalty_plus).setOnClickListener {
            val current = etPenalty.text.toString().toIntOrNull() ?: 0
            etPenalty.setText("${current + 5}")
        }
        dialogView.findViewById<Button>(R.id.btn_penalty_minus).setOnClickListener {
            val current = etPenalty.text.toString().toIntOrNull() ?: 0
            etPenalty.setText("${current - 5}")
        }

        // 저장 버튼 클릭 시
        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val desc = etDesc.text.toString()
            val reward = etReward.text.toString().toIntOrNull() ?: 10
            val penalty = etPenalty.text.toString().toIntOrNull() ?: -10

            if (title.isBlank()) {
                Toast.makeText(context, "규율 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rule == null) {
                // [추가 모드]
                viewModel.addRule(title, desc, reward, penalty)
                Toast.makeText(context, "새 규율이 추가되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // [수정 모드] 기존 ID 유지하면서 내용만 변경
                val updatedRule = rule.copy(
                    title = title,
                    description = desc,
                    defaultScore = reward,
                    penalty = penalty
                )
                viewModel.updateRule(updatedRule)
                Toast.makeText(context, "규율이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // 취소 버튼
        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showDeleteConfirmDialog(rule: Rule) {
        AlertDialog.Builder(requireContext())
            .setTitle("규율 폐기")
            .setMessage("'${rule.title}' 규율을 정말 삭제하시겠습니까?")
            .setPositiveButton("폐기") { _, _ ->
                viewModel.deleteRule(rule)
                Toast.makeText(context, "규율이 폐기되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}