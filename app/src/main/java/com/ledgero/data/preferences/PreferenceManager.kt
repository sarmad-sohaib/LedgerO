package com.ledgero.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class Language {
    ENGLISH,
    URDU
}

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

    val prefFlow: Flow<Int> = context.dataStore.data.map { pref ->

        pref[PreferencesKey.LANGUAGE] ?: 0
    }

    suspend fun updateLanguage(language: Language) {
        context.dataStore.edit { pref ->
            pref[PreferencesKey.LANGUAGE] = language.ordinal
        }
    }

    private object PreferencesKey {
        val LANGUAGE = intPreferencesKey("language")
    }
}