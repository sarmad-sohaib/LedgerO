package com.ledgero.reminders.reminderalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper
import com.ledgero.reminders.data.Reminder
import com.ledgero.reminders.reminders.data.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    @CallSuper
    override fun onReceive(context: Context?, intent: Intent?) {
    }
}

private const val TAG = "AlertReceiver"

@AndroidEntryPoint
class AlertReceiver : HiltBroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        val pendingResult: PendingResult = goAsync()

        scope.launch(Dispatchers.Default) {
            try {
                val reminder = intent?.getParcelableExtra<Reminder>("reminder")
                if (reminder != null) context?.let { ReminderNotificationService(it) }
                    ?.showRemindersNotification(reminder)

                val completedReminder = reminder?.copy(
                    amount = reminder.amount,
                    recipient = reminder.recipient,
                    description = reminder.description,
                    timeStamp = reminder.timeStamp,
                    complete = true,
                    give = reminder.give,
                    id = reminder.id
                )
                reminderRepository.updateReminder(completedReminder!!)
            } finally {
                pendingResult.finish()
            }
        }
    }

}