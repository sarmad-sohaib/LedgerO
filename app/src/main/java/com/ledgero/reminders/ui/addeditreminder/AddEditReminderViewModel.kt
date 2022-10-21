package com.ledgero.reminders.ui.addeditreminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ledgero.reminders.data.Reminder
import com.ledgero.reminders.reminders.data.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddEditReminderViewModel"

sealed class AddEditReminderFragmentEvents  {
    object SaveReminderButtonClicked: AddEditReminderFragmentEvents()
    data class ShowInvalidInputMessage(val msg: String): AddEditReminderFragmentEvents()
    data class NavigateBackOnSaveWithResult(val result: String): AddEditReminderFragmentEvents()
}

@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val state: SavedStateHandle
): ViewModel() {

    private val collectionKey = FirebaseAuth.getInstance().currentUser?.uid.toString() + "Reminders"

    private val _uiEvents = Channel<AddEditReminderFragmentEvents>()
    val uiEvent = _uiEvents.receiveAsFlow()

    //reminder from nav-args
    val reminder = state.get<Reminder>("reminder")

    var reminderAmount = state.get<String>("reminderAmount") ?: reminder?.amount ?: ""
    set(value) {
        field = value
        state["reminderAmount"] = value
    }

    var reminderDescription = state.get<String>("reminderDescription") ?: reminder?.description ?: ""
    set(value) {
        field = value
        state["reminderDescription"] = value
    }

    var reminderRecipient = state.get<String>("reminderRecipient") ?: reminder?.recipient ?: ""
    set(value) {
        field = value
        state["reminderRecipient"] = value
    }

    var reminderTime = state.get<Long>("reminderTime") ?: reminder?.timeStamp ?: 0L
    set(value) {
        field = value
        state["reminderTime"] = value
    }

    var reminderIsGive = state.get<Boolean>("reminderIsGive") ?: reminder?.give ?: false
    set(value) {
        field = value
        state["reminderIsGive"] = value
    }

    fun saveReminderButtonClicked() = viewModelScope.launch {
        _uiEvents.send(AddEditReminderFragmentEvents.SaveReminderButtonClicked)
    }

    fun onSave() = viewModelScope.launch {

        if (reminderAmount.isBlank()
            || reminderAmount.isBlank()
            || reminderTime == 0L
            || reminderRecipient.isBlank()
        ) {
            _uiEvents.send(AddEditReminderFragmentEvents.ShowInvalidInputMessage("Invalid Input"))
        } else {
            if (reminder != null) {
                val updatedReminder = reminder.copy(
                    amount = reminderAmount,
                    recipient = reminderRecipient,
                    description = reminderDescription,
                    timeStamp = reminderTime,
                    give = reminderIsGive,
                    id = reminder.id
                )
                onUpdate(updatedReminder)
            } else {
                val newReminder = Reminder(
                    amount = reminderAmount,
                    recipient = reminderRecipient,
                    description = reminderDescription,
                    complete = false,
                    timeStamp = reminderTime,
                    give = reminderIsGive
                )
                reminderRepository.insertReminder(collectionKey, newReminder)
            }
            _uiEvents.send(AddEditReminderFragmentEvents.NavigateBackOnSaveWithResult("Reminder Saved!!"))
        }
    }

    private fun onUpdate(reminder: Reminder) = viewModelScope.launch {
        reminderRepository.updateReminder(reminder)
        _uiEvents.send(AddEditReminderFragmentEvents.NavigateBackOnSaveWithResult("Reminder Updated!!"))
    }
}