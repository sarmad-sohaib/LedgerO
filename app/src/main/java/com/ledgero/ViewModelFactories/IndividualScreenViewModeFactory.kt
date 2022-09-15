package com.ledgero.ViewModelFactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ledgero.ViewModels.IndividualScreenViewModel
import com.ledgero.Repositories.IndividualScreenRepo


class IndividualScreenViewModeFactory ( val individualScreenRepo: IndividualScreenRepo) :  ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return IndividualScreenViewModel(individualScreenRepo) as T
    }
}