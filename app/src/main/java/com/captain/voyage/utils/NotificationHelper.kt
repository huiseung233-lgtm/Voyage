package com.captain.voyage.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.captain.voyage.R
import com.captain.voyage.ui.main.MainActivity
import java.util.Calendar

object NotificationHelper {

    private const val CHANNEL_ID = "voyage_report_channel"
    private const val ALARM_REQUEST_CODE = 1001

    // 1. 알림 채널 생성 (중요도 HIGH로 변경하여 배너 알림 활성화)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "정기 항해 보고"
            val descriptionText = "설정된 간격마다 항해 보고 알림을 보냅니다."
            // ★ 중요도를 HIGH로 설정해야 다른 앱 사용 중에도 상단 배너가 뜹니다.
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 2. 알림 예약 (기존 동일)
    fun scheduleNotification(context: Context, intervalMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)
        val triggerTime = calculateNextAlarmTime(intervalMinutes)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervalMinutes * 60 * 1000L,
            pendingIntent
        )
    }

    // 3. 알림 취소 (기존 동일)
    fun cancelNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    // 4. 실제 알림 띄우기 (핵심 신호 추가)
    fun showNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // 앱이 이미 켜져 있다면 그 위에 띄우고, 아니면 새로 실행
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // ★ [추가] 액티비티에 보낼 팝업 실행 신호
            putExtra("OPEN_LOGBOOK", true)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("📜 정기 항해 보고")
            .setContentText("선장님, 현재 항해 상태를 점검할 시간입니다.")
            // ★ 우선순위도 HIGH로 설정
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, builder.build())
    }

    private fun calculateNextAlarmTime(intervalMinutes: Int): Long {
        // [안전장치] 간격이 0이거나 음수면 계산이 불가능하므로 기본값 60분으로 처리
        val interval = if (intervalMinutes <= 0) 60 else intervalMinutes
        val calendar = Calendar.getInstance()
        val currentMinute = calendar.get(Calendar.MINUTE)
        val nextMinute = ((currentMinute / interval) + 1) * interval
        if (nextMinute >= 60) {
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, nextMinute - 60)
        } else {
            calendar.set(Calendar.MINUTE, nextMinute)
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.MINUTE, interval)
        }
        return calendar.timeInMillis
    }
}