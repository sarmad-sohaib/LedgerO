package com.ledgero.reminders.ui.completedreminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.reminders.data.Reminder
import com.ledgero.reminders.reminders.data.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CompletedReminderFragmentUiState {
    data class ShowAllCompletedReminders(val list: List<Reminder?>) : CompletedReminderFragmentUiState()
    object Empty : CompletedReminderFragmentUiState()
}

@HiltViewModel
class CompletedReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    init {
        getAllCompletedReminders()
    }

    private val _uiState = MutableStateFlow<CompletedReminderFragmentUiState>(CompletedReminderFragmentUiState.Empty)
    val uiState = _uiState.asStateFlow()

    private fun getAllCompletedReminders() = viewModelScope.launch {
        reminderRepository.getCompletedRemindersStream().collect {
            _uiState.value = CompletedReminderFragmentUiState.ShowAllCompletedReminders(it)
        }
    }
}