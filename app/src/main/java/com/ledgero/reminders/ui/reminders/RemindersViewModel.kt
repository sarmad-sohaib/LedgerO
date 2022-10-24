package com.ledgero.reminders.ui.reminders

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ledgero.reminders.data.Reminder
import com.ledgero.reminders.reminders.data.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RemindersUiState {
    data class AllReminders(val list: List<Reminder?> = emptyList()) : RemindersUiState()
    data class Error(val msg: String) : RemindersUiState()
    object Loading : RemindersUiState()
    object Empty : RemindersUiState()
}

sealed class ReminderUiEvents {
    object NavigateToAddReminderScreen : ReminderUiEvents()
    object NavigateToCompletedRemindersScreen : ReminderUiEvents()
    data class NavigateToEditReminderScreen(val reminder: Reminder) : ReminderUiEvents()
    data class ShowSaveReminderResult(val result: String) : ReminderUiEvents()
    data class ShowUndoDeleteReminderMessage(val reminder: Reminder) : ReminderUiEvents()
    data class CompleteReminder(val reminder: Reminder, val checkboxValue: Boolean) :
        ReminderUiEvents()

    data class ShowReminderCompleteMessage(val msg: String) : ReminderUiEvents()
}

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    init {
        getAllReminders()
    }

    private val _uiState = MutableStateFlow<RemindersUiState>(RemindersUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _uiEvent = Channel<ReminderUiEvents>()
    val uiEvents = _uiEvent.receiveAsFlow()

    private val collectionKey = FirebaseAuth.getInstance().currentUser?.uid.toString() + "Reminders"

    @SuppressLint("SuspiciousIndentation")
    private fun getAllReminders() = viewModelScope.launch {
        viewModelScope.launch {
            reminderRepository.getRemindersStream(collectionKey)
                .collect {
                    if (it.isEmpty()) {
                        _uiState.value = RemindersUiState.Empty
                    } else
                        _uiState.value = RemindersUiState.AllReminders(it)
                }
        }
    }

    fun onReminderClicked(reminder: Reminder) = viewModelScope.launch {
        _uiEvent.send(ReminderUiEvents.NavigateToEditReminderScreen(reminder))
    }

    fun addNewReminderClicked() = viewModelScope.launch {
        _uiEvent.send(ReminderUiEvents.NavigateToAddReminderScreen)
    }

    fun onSaveReminderResult(result: String) = viewModelScope.launch {
        when (result) {
            "Reminder Updated!!" -> _uiEvent.send(ReminderUiEvents.ShowSaveReminderResult(result))
            "Reminder Saved!!" -> _uiEvent.send(ReminderUiEvents.ShowSaveReminderResult(result))
        }
    }

    fun onReminderSwiped(reminder: Reminder) = viewModelScope.launch {
        reminderRepository.deleteReminder(reminder)
        _uiEvent.send(ReminderUiEvents.ShowUndoDeleteReminderMessage(reminder))
    }

    fun onUndoDeleteClicked(reminder: Reminder) = viewModelScope.launch {
        reminderRepository.insertReminder(collectionKey, reminder)
    }

    fun checkBoxCompleteReminderChanged(reminder: Reminder, checkboxValue: Boolean) =
        viewModelScope.launch {
            _uiEvent.send(ReminderUiEvents.CompleteReminder(reminder, checkboxValue))
        }

    fun updateReminder(reminder: Reminder, checkboxValue: Boolean) = viewModelScope.launch {
        val completedReminder = reminder.copy(
            amount = reminder.amount,
            recipient = reminder.recipient,
            description = reminder.description,
            timeStamp = reminder.timeStamp,
            complete = checkboxValue,
            give = reminder.give,
            id = reminder.id
        )
        reminderRepository.updateReminder(completedReminder)
    }

    fun showCompletedRemindersClicked() = viewModelScope.launch {
        _uiEvent.send(ReminderUiEvents.NavigateToCompletedRemindersScreen)
    }

    fun completeReminderClicked() = viewModelScope.launch {
        val msg = "Reminder is completed!!"
        _uiEvent.send(ReminderUiEvents.ShowReminderCompleteMessage(msg))
    }
}