package com.captain.voyage.ui.settings

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.captain.voyage.VoyageApplication
import com.captain.voyage.databinding.FragmentSettingsBinding
import com.captain.voyage.utils.NotificationHelper // [New]
import com.captain.voyage.utils.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // 저장 키값
    private val PREF_NAME = "voyage_settings"
    private val KEY_LIMIT_TIME = "limit_time"
    private val KEY_WAKE_TIME = "wake_time"
    private val KEY_MORNING_BUFFER = "morning_buffer"
    private val KEY_NOTI_ENABLED = "noti_enabled"
    private val KEY_NOTI_INTERVAL = "noti_interval"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [New] 알림 채널 생성 (최초 1회 필요)
        NotificationHelper.createNotificationChannel(requireContext())

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // 시간 설정 불러오기
        val limitStr = prefs.getString(KEY_LIMIT_TIME, "02:00") ?: "02:00"
        val wakeStr = prefs.getString(KEY_WAKE_TIME, "07:00") ?: "07:00"
        val bufferMin = prefs.getInt(KEY_MORNING_BUFFER, 60)

        binding.btnSetLimitTime.text = limitStr
        binding.btnSetWakeTime.text = wakeStr
        binding.etMorningBuffer.setText(bufferMin.toString())

        // 알림 설정 불러오기
        val isNotiEnabled = prefs.getBoolean(KEY_NOTI_ENABLED, false)
        val notiInterval = prefs.getInt(KEY_NOTI_INTERVAL, 60)

        binding.switchNotification.isChecked = isNotiEnabled
        binding.etNotiInterval.setText(notiInterval.toString())
        binding.layoutNotiInterval.visibility = if (isNotiEnabled) View.VISIBLE else View.GONE

        updateTimeManager(limitStr, wakeStr, bufferMin)
    }

    private fun setupListeners() {
        // 시간 설정 (마감)
        binding.btnSetLimitTime.setOnClickListener {
            showTimePicker(binding.btnSetLimitTime.text.toString()) { time ->
                binding.btnSetLimitTime.text = time
                saveSettings()
            }
        }
        // 시간 설정 (기상)
        binding.btnSetWakeTime.setOnClickListener {
            showTimePicker(binding.btnSetWakeTime.text.toString()) { time ->
                binding.btnSetWakeTime.text = time
                saveSettings()
            }
        }

        // 여유 시간 입력
        binding.etMorningBuffer.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) saveSettings()
        }

        // 알림 스위치
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutNotiInterval.visibility = if (isChecked) View.VISIBLE else View.GONE
            saveSettings() // 저장 시 알림 스케줄링도 같이 수행됨
        }

        // 알림 간격 입력
        binding.etNotiInterval.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) saveSettings()
        }

        // 초기화 버튼
        binding.btnResetData.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("🚨 데이터 초기화")
                .setMessage("정말 초기화하시겠습니까?")
                .setPositiveButton("초기화") { _, _ -> resetAllData() }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun showTimePicker(current: String, onSelected: (String) -> Unit) {
        val parts = current.split(":")
        TimePickerDialog(requireContext(), { _, h, m ->
            onSelected(String.format("%02d:%02d", h, m))
        }, parts[0].toInt(), parts[1].toInt(), true).show()
    }

    private fun saveSettings() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val buffer = binding.etMorningBuffer.text.toString().toIntOrNull() ?: 60
        val interval = binding.etNotiInterval.text.toString().toIntOrNull() ?: 60
        val isNotiEnabled = binding.switchNotification.isChecked

        prefs.edit().apply {
            putString(KEY_LIMIT_TIME, binding.btnSetLimitTime.text.toString())
            putString(KEY_WAKE_TIME, binding.btnSetWakeTime.text.toString())
            putInt(KEY_MORNING_BUFFER, buffer)
            putBoolean(KEY_NOTI_ENABLED, isNotiEnabled)
            putInt(KEY_NOTI_INTERVAL, interval)
            apply()
        }

        // TimeManager 업데이트
        updateTimeManager(
            binding.btnSetLimitTime.text.toString(),
            binding.btnSetWakeTime.text.toString(),
            buffer
        )

        // [New] 알림 스케줄링 업데이트
        if (isNotiEnabled) {
            // 켜졌으면 -> 새로운 간격으로 예약
            NotificationHelper.scheduleNotification(requireContext(), interval)
            Toast.makeText(context, "${interval}분 간격으로 알림이 설정되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 꺼졌으면 -> 예약 취소
            NotificationHelper.cancelNotification(requireContext())
            Toast.makeText(context, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTimeManager(limitStr: String, wakeStr: String, buffer: Int) {
        try {
            TimeManager.LIMIT_TIME = LocalTime.parse(limitStr)
            TimeManager.WAKE_UP_TIME = LocalTime.parse(wakeStr)
            TimeManager.MORNING_BUFFER_MINUTES = buffer.toLong()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun resetAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            (requireActivity().application as VoyageApplication).database.clearAllTables()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "초기화 완료", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}