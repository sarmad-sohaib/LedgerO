package com.ledgero.cashregister.di

import com.ledgero.cashregister.EntriesRepositoryImpl
import com.ledgero.cashregister.data.EntryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CashRegisterHiltModule {

    @Provides
    fun providesEntryRepository(entriesRepositoryImpl: EntriesRepositoryImpl):
            EntryRepository = entriesRepositoryImpl
}