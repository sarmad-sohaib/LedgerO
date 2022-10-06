package com.ledgero.reminders.di

import com.ledgero.reminders.reminders.data.ReminderRepository
import com.ledgero.reminders.reminders.data.ReminderRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RemindersModule {

    @Provides
    fun providesReminderRepository(reminderRepositoryImpl: ReminderRepositoryImpl):
            ReminderRepository = reminderRepositoryImpl

}