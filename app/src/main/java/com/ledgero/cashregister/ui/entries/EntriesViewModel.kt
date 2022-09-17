package com.ledgero.cashregister

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.cashregister.data.Entry
import com.ledgero.cashregister.data.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CashRegisterEvents {
    object NavigateToCashOutEntryScreen: CashRegisterEvents()
    object NavigateToCashInEntryScreen: CashRegisterEvents()
    data class ShowUndoDeletedEntryMessage(val entry: Entry): CashRegisterEvents()
    data class NavigateToEditEntryScreen(val entry: Entry): CashRegisterEvents()
    data class ShowTaskSavedConfirmationMessage(val msg: String): CashRegisterEvents()
}

@HiltViewModel
class EntriesViewModel @Inject constructor(
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val cashRegisterEventsChannel = Channel<CashRegisterEvents>()
    val cashRegisterEvents = cashRegisterEventsChannel.receiveAsFlow()

    fun getEntries(uId: String, myCallback: MyCallback) = viewModelScope.launch {
        entryRepository.getEntries(uId, myCallback)
    }

    fun cashInButtonClicked() = viewModelScope.launch {
        cashRegisterEventsChannel.send(CashRegisterEvents.NavigateToCashInEntryScreen)
    }

    fun cashOutButtonClicked() = viewModelScope.launch {
        cashRegisterEventsChannel.send(CashRegisterEvents.NavigateToCashOutEntryScreen)
    }

    fun onEntrySwiped(uId: String, id: String, entry: Entry) = viewModelScope.launch {
        entryRepository.deleteEntry(uId, id)
        cashRegisterEventsChannel.send(CashRegisterEvents.ShowUndoDeletedEntryMessage(entry))
    }

    fun onUndoDeleteClick(uId: String, entry: Entry) = viewModelScope.launch {
        entryRepository.insertEntry(uId, entry)
    }

    fun onEntrySelected(entry: Entry) = viewModelScope.launch {
        cashRegisterEventsChannel.send(CashRegisterEvents.NavigateToEditEntryScreen(entry))
    }

    fun onAddEditResult(result: Int) {
        when(result) {
            ADD_ENTRY_RESULT -> showTaskSavedMessage("Task Saved!")
            EDIT_ENTRY_RESULT -> showTaskSavedMessage("Task Updated!")
        }
    }

    private fun showTaskSavedMessage(msg: String) = viewModelScope.launch {
        cashRegisterEventsChannel.send(CashRegisterEvents.ShowTaskSavedConfirmationMessage(msg))
    }
}