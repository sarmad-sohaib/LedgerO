package com.ledgero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ledgero.reminders.ui.ReminderNotificationService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LedgeroApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        createReminderNotificationChannel()
    }

    private fun createReminderNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel (
                ReminderNotificationService.REMINDERS_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH,
                    )
            reminderChannel.description = "Reminder's Notifications"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }
}