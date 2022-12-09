package com.ledgero.more.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.data.preferences.Language
import com.ledgero.data.preferences.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MoreViewModel"

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
): ViewModel() {

    val prefFlow = preferenceManager.prefFlow

    fun update(language: Language) {
        viewModelScope.launch {
            preferenceManager.updateLanguage(language)
        }
    }
}