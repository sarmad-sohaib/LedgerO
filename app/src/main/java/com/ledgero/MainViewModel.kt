package com.ledgero

import androidx.lifecycle.ViewModel
import com.ledgero.data.preferences.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
): ViewModel() {

    val prefFlow = preferenceManager.prefFlow
}