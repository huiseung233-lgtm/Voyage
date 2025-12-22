package com.captain.voyage.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 시간이 되었다! 알림 띄우기 요청
        NotificationHelper.showNotification(context)
    }
}