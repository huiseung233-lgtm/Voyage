package com.captain.voyage.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class AlarmReceiver : BroadcastReceiver() {
    
    private val PREF_NAME = "voyage_settings"
    private val KEY_NOTI_ENABLED = "noti_enabled"
    private val KEY_NOTI_INTERVAL = "noti_interval"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Timber.d("⚓ AlarmReceiver received action: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // 1. 재부팅 시 알람 재등록
            reScheduleAlarm(context)
        } else {
            // 2. 일반 알람 수신
            handleAlarm(context, intent)
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        Timber.d("⚓ Alarm triggered!")
        
        // 알림 표시
        NotificationHelper.showNotification(context)

        // 다음 알람 예약 (연쇄 반응)
        val interval = intent.getIntExtra("INTERVAL", -1)
        if (interval > 0) {
            Timber.d("⚓ Rescheduling next alarm in $interval minutes")
            NotificationHelper.scheduleNotification(context, interval)
        } else {
            // 인텐트에 간격이 없는 경우를 대비해 저장소에서 읽어옴
            reScheduleAlarm(context)
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
