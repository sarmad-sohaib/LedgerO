package com.ledgero.ViewModelFactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ledgero.DataClasses.Entries
import com.ledgero.Repositories.CanceledEntriesRepo
import com.ledgero.Repositories.EditSingleEntriesRepo
import com.ledgero.ViewModels.CanceledEntriesViewModel
import com.ledgero.ViewModels.EditSingleEntriesViewModel

class EditSingleEntriesViewModelFactory ( val editSingleEntriesRepo: EditSingleEntriesRepo ,
                                          val currentEntry:Entries) :  ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditSingleEntriesViewModel(editSingleEntriesRepo,currentEntry) as T
    }
}