package com.ledgero.reminders.di

import com.ledgero.reminders.data.ReminderRepositoryImpl
import com.ledgero.reminders.reminders.data.ReminderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RemindersModule {

    @Provides
    fun  providesReminderRepository(reminderRepositoryImpl: ReminderRepositoryImpl):
            ReminderRepository = reminderRepositoryImpl

}