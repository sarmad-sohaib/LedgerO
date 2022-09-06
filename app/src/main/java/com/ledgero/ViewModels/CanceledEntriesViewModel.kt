package com.ledgero.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.Repositories.CanceledEntriesRepo

class CanceledEntriesViewModel(private val canceledEntriesRepo: CanceledEntriesRepo) : ViewModel() {

    var allCanceledEntries: LiveData<ArrayList<Entries>>


    init {
        allCanceledEntries = getEntriesFromRepo()
    }

    fun getCanceledEntries(): LiveData<ArrayList<Entries>> {

        return allCanceledEntries

    }

    fun deleteEntry(pos: Int) {
        canceledEntriesRepo.deleteEntry(pos)
    }

    private fun getEntriesFromRepo(): LiveData<ArrayList<Entries>> {

        return canceledEntriesRepo.getEntries()
    }

    fun removeListener() {
        canceledEntriesRepo.removeListener()
    }

    fun requestAgain(pos: Int) {
        canceledEntriesRepo.requestAgain(pos)
    }
}