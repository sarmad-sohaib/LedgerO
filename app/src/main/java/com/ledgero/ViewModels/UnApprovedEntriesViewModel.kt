package com.ledgero.ViewModels

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.Repositories.UnApprovedEntriesRepo
import com.ledgero.UtillClasses.AddEntryWithVoice_EditEntry
import com.ledgero.UtillClasses.AddEntry_EditEntry
import com.ledgero.UtillClasses.DeleteEntryWithVoice_EditEntry
import com.ledgero.UtillClasses.DeleteEntry_EditEntry
import com.ledgero.other.Constants
import com.ledgero.pushnotifications.PushNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import com.ledgero.utils.Utill_SingleLedgerMetaData


class UnApprovedEntriesViewModel( val unApprovedEntriesRepo: UnApprovedEntriesRepo,ledgerUID:String) : ViewModel() {

    var allUnApprovedEntries: LiveData<ArrayList<Entries>>
    var mledgerUID=ledgerUID
    private val pushNotificationInterface = PushNotification()


    private var ledgerMetaDataUtill= Utill_SingleLedgerMetaData(ledgerUID)
    private var TAG="UnApprovedEntriesViewModel"

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
   fun EditEntryaccepted(oldentry: Entries,newEntry: Entries){
        //check if entry to be delete has voice not or not

        if(oldentry.hasVoiceNote!!){
            CoroutineScope(Dispatchers.Default).async{


                DeleteEntryWithVoice_EditEntry(mledgerUID,oldentry,newEntry).deleteEntry(oldentry)
                AddEntryWithVoice_EditEntry(mledgerUID,oldentry,newEntry).addEditedEntryAsNewEntry(newEntry)




            }
        }else{



            var isEditied=   CoroutineScope(Dispatchers.Default).async{


                  DeleteEntry_EditEntry(mledgerUID,oldentry,newEntry).deleteEntry(oldentry)
                AddEntry_EditEntry(mledgerUID,oldentry,newEntry).addEditedEntryAsNewEntry(newEntry)




            }

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


        unApprovedEntriesRepo.rejectedEntry(entry)
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



fun approveAll(){

    allUnApprovedEntries.let {
        if (allUnApprovedEntries.value != null){




            var size= allUnApprovedEntries.value!!.size
            var index=0;
            while (index<size){

                if (allUnApprovedEntries.value!![index].entryMadeBy_userID.equals(User.userID)) {
                    Log.d(TAG, "approveAll: waiitng for approval")
                }else{

                // means receiver has accepted the request made by requester
                if (allUnApprovedEntries.value!![index].requestMode == Constants.ADD_REQUEST_REQUEST_MODE)//request to add entry
                {
                    approveEntry(index)

                }
                if (allUnApprovedEntries.value!![index].requestMode == Constants.DELETE_REQUEST_REQUEST_MODE) {//request to Delete entry
                    //means receiver accepted to delete a entry from ledger
                    deleteEntryFromLedgerRequest_accepted(index)
                }
                if (allUnApprovedEntries.value!![index].requestMode == Constants.EDIT_REQUEST_REQUEST_MODE) {

                    var oldEntry = Entries()
                    val newEntry =allUnApprovedEntries.value!![index]

                    for (currentLedger in User.getUserSingleLedgers()!!) {
                        if (currentLedger.ledgerUID!! == mledgerUID) {
                            for (entry in currentLedger.entries!!) {
                                if (allUnApprovedEntries.value!![index].entryUID!! == entry.entryUID) {
                                    oldEntry = entry
                                }
                            }
                        }
                    } // accessing entry to be deleted from ledger approved entries

               EditEntryaccepted(oldEntry, newEntry)

                }


                pushNotificationInterface.createAndSendNotification(mledgerUID,
                    Constants.ENTRY_APPROVED)
                }
                index++
            }  

        }
    }
}

 }
