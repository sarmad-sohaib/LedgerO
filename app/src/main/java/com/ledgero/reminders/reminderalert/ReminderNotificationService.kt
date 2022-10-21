package com.ledgero.reminders.reminderalert

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import com.ledgero.R
import com.ledgero.reminders.RemindersMainActivity
import com.ledgero.reminders.data.Reminder

class ReminderNotificationService (
    private val context: Context
        )
{

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val REMINDERS_CHANNEL_ID = "reminder-channel"
    }

    fun showRemindersNotification(reminder: Reminder) {

        val text = "You have to" + if (reminder.give == true) " give " else " take " + reminder.amount + " " + "from" + " " + reminder.recipient

        val activityIntent = Intent(context, RemindersMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            if(SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val reminderNotification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clock_wait)
            .setContentTitle(reminder.amount + " for " + reminder.description)
            .setContentText(text)
            .setStyle(BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, reminderNotification )
    }
}