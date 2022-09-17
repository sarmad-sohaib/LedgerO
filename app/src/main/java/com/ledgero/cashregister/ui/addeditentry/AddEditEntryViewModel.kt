package com.ledgero.cashregister.ui.addeditentry

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ledgero.cashregister.ADD_ENTRY_RESULT
import com.ledgero.cashregister.EDIT_ENTRY_RESULT
import com.ledgero.cashregister.data.Entry
import com.ledgero.cashregister.data.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddEditEntryEvents {
    data class ShowInvalidInputMessage(val msg: String) : AddEditEntryEvents()
    data class NavigateBackWithResult(val result: Int): AddEditEntryEvents()
}

@HiltViewModel
class AddEditEntryViewModel @Inject constructor (
    private val entryRepository: EntryRepository,
    private val state: SavedStateHandle
): ViewModel() {

    private val addEditEntryEventsChannel = Channel<AddEditEntryEvents>()
    val addEditEntryEvent = addEditEntryEventsChannel.receiveAsFlow()

    val uId = FirebaseAuth.getInstance().currentUser?.uid

    fun onSaveClick(isCashOut: Boolean) {
        if (entryAmount.isBlank()) {
            showInvalidInputMessage("Invalid amount")
            return
        }

        if (entry != null) {

            val updatedEntry = entry.copy(
                amount = entryAmount,
                description = entryDescription,
                id = entry.id
            )

            updateEntry(uId, updatedEntry.id, updatedEntry)

            Log.i("ENTRY", "onSaveClick: ${updatedEntry}")
        } else {

            val newEntry = Entry(
                entryAmount,
                entryDescription,
                isCashOut
            )
            if (uId != null) {
                createEntry(uId, newEntry)
            }
        }
    }

    private fun updateEntry(uId: String?, id: String?, entry: Entry) = viewModelScope.launch {
        entryRepository.updateEntry(uId!!, id!!, entry)
        addEditEntryEventsChannel.send(AddEditEntryEvents.NavigateBackWithResult(EDIT_ENTRY_RESULT))
    }

    private fun showInvalidInputMessage(msg: String) = viewModelScope.launch {
        addEditEntryEventsChannel.send(AddEditEntryEvents.ShowInvalidInputMessage(msg = msg))
    }

    private fun createEntry(uId: String, newEntry: Entry) = viewModelScope.launch {
        entryRepository.insertEntry(uId, newEntry)
        addEditEntryEventsChannel.send(AddEditEntryEvents.NavigateBackWithResult(ADD_ENTRY_RESULT))
    }

    val entry = state.get<Entry>("task")

    var entryAmount = state.get<String>("entryAmount") ?: entry?.amount ?:""
    set(value) {
        field = value
        state["entryAmount"] = value
    }

    var entryDescription = state.get<String>("entryDescription") ?: entry?.description ?: ""
    set(value) {
        field = value
        state["entryDescription"] = value
    }
}