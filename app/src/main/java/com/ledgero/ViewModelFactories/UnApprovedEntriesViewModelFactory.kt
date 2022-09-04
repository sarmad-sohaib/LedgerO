package com.ledgero.ViewModelFactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ledgero.Repositories.UnApprovedEntriesRepo
import com.ledgero.ViewModels.IndividualScreenViewModel
import com.ledgero.ViewModels.UnApprovedEntriesViewModel

class UnApprovedEntriesViewModelFactory( val unApprovedEntriesRepo: UnApprovedEntriesRepo) :  ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UnApprovedEntriesViewModel(unApprovedEntriesRepo) as T
    }
}