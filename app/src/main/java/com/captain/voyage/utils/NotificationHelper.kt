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
    const val ALARM_REQUEST_CODE = 1001

    // 1. 알림 채널 생성
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "정기 항해 보고"
            val descriptionText = "설정된 간격마다 항해 보고 알림을 보냅니다."
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

    // 2. 알림 예약
    fun scheduleNotification(context: Context, intervalMinutes: Int) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 권한 체크 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return 
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("INTERVAL", intervalMinutes)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)
        
        // 정각 기준 다음 알람 시간 계산
        val triggerTime = calculateNextAlarmTime(intervalMinutes)

        try {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 다음 정각 시간 계산 로직
    private fun calculateNextAlarmTime(intervalMinutes: Int): Long {
        val interval = if (intervalMinutes <= 0) 60 else intervalMinutes
        val calendar = Calendar.getInstance()
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        // 다음 정각 분 계산 (예: 13분, 간격 10분 -> 20분)
        var nextMinute = ((currentMinute / interval) + 1) * interval
        
        // 초, 밀리초 초기화
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (nextMinute >= 60) {
            // 60분을 넘어가면 시간을 증가시키고 분을 조정 (예: 53분, 간격 10분 -> 60분 -> 다음 시간 00분)
            // nextMinute가 60, 70 등이 될 수 있음
            val hoursToAdd = nextMinute / 60
            val minutesRemainder = nextMinute % 60
            
            calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd)
            calendar.set(Calendar.MINUTE, minutesRemainder)
        } else {
            calendar.set(Calendar.MINUTE, nextMinute)
        }
        
        // 혹시 계산된 시간이 이미 지났다면(거의 없을 테지만), 다음 주기로 밀기
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.MINUTE, interval)
        }

        return calendar.timeInMillis
    }

    // 3. 알림 취소
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

    // 4. 실제 알림 띄우기
    fun showNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            notificationManager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}