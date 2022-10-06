package com.ledgero.reminders.reminders.data

import kotlinx.coroutines.flow.Flow

interface ReminderRepository {


    suspend fun getRemindersStream(collectionKey: String): Flow<List<Reminder?>>

    suspend fun insertReminder(collectionKey: String, reminder: Reminder)

    suspend fun updateReminder(reminder: Reminder)

    suspend fun deleteReminder(reminder: Reminder)

}