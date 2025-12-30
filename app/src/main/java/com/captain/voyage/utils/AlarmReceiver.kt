package com.captain.voyage.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {
    
    private val PREF_NAME = "voyage_settings"
    private val KEY_NOTI_ENABLED = "noti_enabled"
    private val KEY_NOTI_INTERVAL = "noti_interval"
    
    // [New] Quiet Hours Keys
    private val KEY_QUIET_ENABLED = "quiet_enabled"
    private val KEY_QUIET_START = "quiet_start"
    private val KEY_QUIET_END = "quiet_end"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Timber.d("⚓ AlarmReceiver received action: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            reScheduleAlarm(context)
        } else {
            handleAlarm(context, intent)
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        Timber.d("⚓ Alarm triggered!")
        
        // 1. 방해 금지 시간 체크
        if (shouldSkipNotification(context)) {
            Timber.i("🤫 Shhh... It's quiet hours. Skipping notification.")
        } else {
            // 알림 표시
            NotificationHelper.showNotification(context)
        }

        // 2. 다음 알람 예약 (연쇄 반응 - 항상 수행)
        val interval = intent.getIntExtra("INTERVAL", -1)
        if (interval > 0) {
            Timber.d("⚓ Rescheduling next alarm in $interval minutes")
            NotificationHelper.scheduleNotification(context, interval)
        } else {
            reScheduleAlarm(context)
        }
    }

    private fun shouldSkipNotification(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isQuietEnabled = prefs.getBoolean(KEY_QUIET_ENABLED, false)
        
        if (!isQuietEnabled) return false

        val startStr = prefs.getString(KEY_QUIET_START, "23:00") ?: "23:00"
        val endStr = prefs.getString(KEY_QUIET_END, "07:00") ?: "07:00"

        return try {
            val now = LocalTime.now()
            val start = LocalTime.parse(startStr)
            val end = LocalTime.parse(endStr)

            if (start.isBefore(end)) {
                // Case 1: 당일 설정 (예: 09:00 ~ 12:00)
                // Start <= Now < End (종료 시간은 포함하지 않는 것이 일반적이나 여기선 포함 여부 유연하게)
                now.isAfter(start) && now.isBefore(end)
            } else {
                // Case 2: 밤샘 설정 (예: 23:00 ~ 07:00)
                // Now >= Start OR Now < End
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse quiet hours")
            false
        }
    }

    private fun reScheduleAlarm(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_NOTI_ENABLED, false)
        val interval = prefs.getInt(KEY_NOTI_INTERVAL, 60)

        if (isEnabled) {
            Timber.d("⚓ Re-scheduling alarm: $interval min")
            NotificationHelper.scheduleNotification(context, interval)
        }
    }
}