package com.captain.voyage.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.captain.voyage.service.ScoreOverlayService
import timber.log.Timber
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {
    
    private val PREF_NAME = "voyage_settings"
    private val KEY_NOTI_ENABLED = "noti_enabled"
    private val KEY_NOTI_INTERVAL = "noti_interval"
    private val KEY_QUIET_ENABLED = "quiet_enabled"
    private val KEY_QUIET_START = "quiet_start"
    private val KEY_QUIET_END = "quiet_end"
    
    // [New] Overlay Key
    private val KEY_OVERLAY_ENABLED = "overlay_enabled"

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
            // 2. 오버레이 사용 여부 체크
            if (shouldShowOverlay(context)) {
                Timber.d("✨ Launching Overlay Service")
                val serviceIntent = Intent(context, ScoreOverlayService::class.java)
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        // 백그라운드에서 서비스 시작 제약이 있지만, 오버레이 권한이 있으면 허용될 수 있음
                        // 안전하게 startForegroundService 사용 고려해야 하나, Notification을 띄워야 해서 복잡해짐.
                        // 일단 startService 시도.
                        context.startService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to start overlay service, fallback to notification")
                    NotificationHelper.showNotification(context)
                }
            } else {
                // 3. 알림 표시
                NotificationHelper.showNotification(context)
            }
        }

        // 4. 다음 알람 예약 (항상 수행)
        val interval = intent.getIntExtra("INTERVAL", -1)
        if (interval > 0) {
            Timber.d("⚓ Rescheduling next alarm in $interval minutes")
            NotificationHelper.scheduleNotification(context, interval)
        } else {
            reScheduleAlarm(context)
        }
    }

    private fun shouldShowOverlay(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val useOverlay = prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
        return useOverlay && Settings.canDrawOverlays(context)
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
                now.isAfter(start) && now.isBefore(end)
            } else {
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
