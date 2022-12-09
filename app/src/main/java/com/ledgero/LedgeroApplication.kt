package com.ledgero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.ledgero.data.preferences.Language
import com.ledgero.reminders.reminderalert.ReminderNotificationService
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

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