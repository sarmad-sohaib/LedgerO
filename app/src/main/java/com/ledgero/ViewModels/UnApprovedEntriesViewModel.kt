package com.ledgero.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.DataClasses.Entries
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.Repositories.UnApprovedEntriesRepo
import com.ledgero.UtillClasses.Utill_SingleLedgerMetaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnApprovedEntriesViewModel( val unApprovedEntriesRepo: UnApprovedEntriesRepo,ledgerUID:String) : ViewModel() {

    var allUnApprovedEntries: LiveData<ArrayList<Entries>>
    var mledgerUID=ledgerUID

    private var ledgerMetaDataUtill= Utill_SingleLedgerMetaData(ledgerUID)

    init {
        allUnApprovedEntries= getEntriesFromRepo()
    }

    fun getEntries(): LiveData<ArrayList<Entries>> {

        return allUnApprovedEntries

    }

    private fun getEntriesFromRepo(): LiveData<ArrayList<Entries>> {

        return unApprovedEntriesRepo.getEntries()
    }

    //this function will be called when entry has to be deleted by requester
    fun deleteEntry(pos : Int){
        if (allUnApprovedEntries.value!!.get(pos).hasVoiceNote!!){
            //deleteEntry from unapproved if it has voice note
            unApprovedEntriesRepo.deleteEntry(allUnApprovedEntries.value!!.get(pos))
        }else{
            //deleteEntry from unapproved if it does not have voice note
        unApprovedEntriesRepo.deleteEntry(pos)
    }}


    //this will be called when receiver has accepted requester entry add request
    fun approveEntry(pos:Int){
        // we don't need to check if entry has voice not while approving the adding a new entry
        // because we will be downloading voice if not present in device when user
        // open each entry for edit/review
        updateLedgerMetaData(allUnApprovedEntries.value!!.get(pos))

        unApprovedEntriesRepo.approveEntry(pos)


    }

    //this function will be called when receiver has accepted the request to delete a entry from ledger
    fun deleteEntryFromLedgerRequest_accepted(pos: Int){
        //check if entry to be delete has voice not or not
        var entry = allUnApprovedEntries.value!!.get(pos)

        if(entry.hasVoiceNote!!){
            unApprovedEntriesRepo.acceptDeleteEntryRequestFromApprovedEntries_withVoice(entry)
        }else{


            updateLedgerMetaDataAfterEntryDelete(allUnApprovedEntries.value!!.get(pos))
            unApprovedEntriesRepo.deleteEntry(pos)
        }


    }

    //this function will be called when receiver has accepted the request to Edit a entry from ledger
    //so first we need to delete the old entry
    suspend fun deleteEntryFromLedgerRequest_EditEntryaccepted(oldentry: Entries){
        //check if entry to be delete has voice not or not
        var entry = oldentry

        if(entry.hasVoiceNote!!){
            unApprovedEntriesRepo.deleteEntry_EditEntry_withVoice(entry)
        }else{


            updateLedgerMetaDataAfterEntryDelete_EditEntry(oldentry)
            unApprovedEntriesRepo.deleteEntryToReplaceNewEditiedEntry(oldentry)
        }


    }



    //this will be called when receiver has rejected the requester entry add/del request
    fun rejectEntry(pos:Int){

        //check if rejected Entry was new entry or was approved entry requested for delete/update
        var entry = allUnApprovedEntries.value!!.get(pos)

        if (entry.requestMode==1)//means it was just a new entry requested to add, so we can simply delete it from un-approved and move it to canceled
        {
            unApprovedEntriesRepo.rejectNewAddedEntry(entry)
        }
        if (entry.requestMode==2){
            // means it was a approved Entry requested to delete
            unApprovedEntriesRepo.rejectDeleteRequestForApprovedEntry(pos)

        }


       // unApprovedEntriesRepo.rejectedEntry(entry)
    }

    fun deleteUnApprovedEntryThenUpdateLedgerEntry(pos: Int) {
        unApprovedEntriesRepo.deleteUnApprovedEntryThenUpdateLedgerEntry(pos)

    }



    fun removeListener(){
        unApprovedEntriesRepo.removeListener()
    }

    fun updateLedgerMetaData(entry: Entries){
        ledgerMetaDataUtill.updateTotalAmount_newEntryAdded(entry)

    }
    fun updateLedgerMetaDataAfterEntryDelete(entry: Entries){
        ledgerMetaDataUtill.updateTotalAmount_approvedEntryDeleted(entry)

    }
  suspend  fun updateLedgerMetaDataAfterEntryDelete_EditEntry(entry: Entries){
        ledgerMetaDataUtill.updateTotalAmount_approvedEntryDeleted(entry)

    }


    // edit request functions
 fun updateEntryApproved_withoutVoiceUpdate(pos:Int){
        //1st- delete this entry from approved list
        //2nd- Add this new entry as updated
        //3rd- update ledger meta data

     var job=   viewModelScope.launch(Dispatchers.IO) {
            deleteEntryFromLedgerRequest_accepted(pos)
        }

viewModelScope.launch(Dispatchers.IO) {
    job.join()

    unApprovedEntriesRepo.approveEntry(pos)
    updateLedgerMetaData(allUnApprovedEntries.value!!.get(pos))

}

 }
}