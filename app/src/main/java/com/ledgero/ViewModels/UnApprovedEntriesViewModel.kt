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

        //check if rejected Entry was new entry or was approved entry requested for delete/update
        var entry = allUnApprovedEntries.value!!.get(pos)

        if (entry.requestMode==1)//means it was just a new entry requested to add, so we can simply delete it from un-approved and move it to canceled
        {
            unApprovedEntriesRepo.rejectNewAddedEntry(pos)
        }
        if (entry.requestMode==2){
            // means it was a approved Entry requested to delete
            unApprovedEntriesRepo.rejectDeleteRequestForApprovedEntry(pos)

        }


        unApprovedEntriesRepo.rejectedEntry(pos)
    }

    fun deleteUnApprovedEntryThenUpdateLedgerEntry(pos: Int) {
        unApprovedEntriesRepo.deleteUnApprovedEntryThenUpdateLedgerEntry(pos)

    }

}