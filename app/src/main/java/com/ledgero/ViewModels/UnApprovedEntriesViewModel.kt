package com.ledgero.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.Repositories.UnApprovedEntriesRepo

class UnApprovedEntriesViewModel(private val unApprovedEntriesRepo: UnApprovedEntriesRepo) : ViewModel() {

    var allUnApprovedEntries: LiveData<ArrayList<Entries>>


    init {
        allUnApprovedEntries= getEntriesFromRepo()
    }

    fun getEntries(): LiveData<ArrayList<Entries>> {

        return allUnApprovedEntries

    }

    fun deleteEntry(pos : Int){
        unApprovedEntriesRepo.deleteEntry(pos)
    }
    private fun getEntriesFromRepo(): LiveData<ArrayList<Entries>> {

        return unApprovedEntriesRepo.getEntries()
    }

    fun removeListener(){
        unApprovedEntriesRepo.removeListener()
    }

    fun approveEntry(pos:Int){
        unApprovedEntriesRepo.approveEntry(pos)
    }

    fun rejectEntry(pos:Int){
        unApprovedEntriesRepo.rejectedEntry(pos)
    }

}