package com.captain.voyage.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.captain.voyage.databinding.ItemCalendarDayBinding
import java.time.LocalDate

// 달력의 하루 정보를 담는 데이터 클래스
data class CalendarDay(
    val date: String,    // 날짜 문자열 (YYYY-MM-DD)
    val day: Int,        // 일 (1, 2, 3...)
    val score: Int?,     // 점수 (없으면 null)
    val isEmpty: Boolean // 빈 칸 여부 (1일 앞의 공백 등)
)

class CalendarAdapter(
    private val onClick: (String) -> Unit // 날짜 클릭 시 실행할 함수
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var dayList: List<CalendarDay> = emptyList()
    private val today = LocalDate.now().toString() // 오늘 날짜

    fun submitList(list: List<CalendarDay>) {
        dayList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(dayList[position])
    }

    override fun getItemCount(): Int = dayList.size

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CalendarDay) {
            if (item.isEmpty) {
                // 빈 칸이면 카드 자체를 안 보이게 숨김
                binding.cvDayRoot.visibility = View.INVISIBLE
                binding.cvDayRoot.setOnClickListener(null)
                return
            }

            // 내용 표시
            binding.cvDayRoot.visibility = View.VISIBLE
            binding.tvDayNumber.text = item.day.toString()

            // 점수 표시 (0점이거나 없으면 숨김)
            if (item.score != null && item.score != 0) {
                binding.tvDayScore.text = "${item.score} P"
                binding.tvDayScore.isVisible = true

                // 점수가 높으면 색상 변경 (예: 100점 이상 금색)
                binding.tvDayScore.setTextColor(if (item.score >= 100) Color.parseColor("#FBC02D") else Color.parseColor("#E65100"))
            } else {
                binding.tvDayScore.isVisible = false
            }

            // 오늘 날짜 표시 (테두리 등)
            binding.viewTodayIndicator.isVisible = (item.date == today)

            // 클릭 이벤트
            binding.cvDayRoot.setOnClickListener {
                onClick(item.date)
            }
        }
    }
}