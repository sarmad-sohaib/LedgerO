package com.ledgero.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ledgero.reminders.reminders.data.Reminder
import com.ledgero.reminders.ui.ReminderNotificationService

private const val TAG = "AlertReceiver"

class AlertReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val reminder = intent?.getParcelableExtra<Reminder>("reminder")
        if (reminder != null) {

            context?.let { ReminderNotificationService(it) }?.showRemindersNotification(reminder)

            Log.i(TAG, "onReceive: ${reminder.recipient}")
        }
    }

}