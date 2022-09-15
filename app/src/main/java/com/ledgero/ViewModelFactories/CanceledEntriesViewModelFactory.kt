package com.ledgero.ViewModelFactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ledgero.Repositories.CanceledEntriesRepo
import com.ledgero.ViewModels.CanceledEntriesViewModel

class CanceledEntriesViewModelFactory( val canceledEntriesRepo: CanceledEntriesRepo) :  ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CanceledEntriesViewModel(canceledEntriesRepo) as T
    }
}