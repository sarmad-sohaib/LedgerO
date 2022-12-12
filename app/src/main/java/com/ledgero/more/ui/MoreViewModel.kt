package com.ledgero.more.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.data.preferences.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MoreViewModel"

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    init {
    }

    val prefFlow = preferenceManager.prefFlow

    var key = 0

    fun keyFlow() {
        viewModelScope.launch {
            prefFlow.collect {
                key = it
            }
        }
    }

    fun update(key: Int) {
        viewModelScope.launch {
            preferenceManager.updateTheme(key)
        }
    }
}