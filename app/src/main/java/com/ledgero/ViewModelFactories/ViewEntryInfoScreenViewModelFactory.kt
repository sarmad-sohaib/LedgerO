package com.ledgero.ViewModelFactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.Repositories.ViewEntryInfoScreenRepo
import com.ledgero.ViewModels.IndividualScreenViewModel
import com.ledgero.ViewModels.ViewEntryInfoScreenViewModel

class ViewEntryInfoScreenViewModelFactory( val viewEntryInfoScreenRepo: ViewEntryInfoScreenRepo)
    :  ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewEntryInfoScreenViewModel(viewEntryInfoScreenRepo) as T
    }}