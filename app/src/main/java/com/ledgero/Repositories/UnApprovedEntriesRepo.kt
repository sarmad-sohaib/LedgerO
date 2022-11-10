package com.ledgero.Repositories

import android.content.ContextWrapper
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import com.ledgero.daos.UnApproveEntriesDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
import java.io.File

class UnApprovedEntriesRepo(private val unApproveEntriesDAO: UnApproveEntriesDAO)  {


    fun getEntries(): LiveData<ArrayList<Entries>> {

        return unApproveEntriesDAO.getUnApprovedEntries()

    }

    fun removeListener(){
       unApproveEntriesDAO.removeListener()
    }

    fun rejectNewAddedEntry(entry: Entries){


        unApproveEntriesDAO.entryRejected(entry)
    }

    fun rejectDeleteRequestForApprovedEntry(pos:Int){
        unApproveEntriesDAO.rejectDeleteRequestForApprovedEntry(pos)
    }


    fun deleteEntry(entry: Entries){
        unApproveEntriesDAO.deleteNewEntryAddRequestFromUnApprovedWithVoice(entry)
    }

    //this function is called when receiver accept the request to delete a entry from approved ledger entries
    fun deleteEntry(pos: Int){
        unApproveEntriesDAO.deleteEntryApproved(pos)
    }
    //this function is called when receiver accept the request to Edit a entry from approved ledger entries
    fun deleteEntryToReplaceNewEditiedEntry(entry: Entries){
        unApproveEntriesDAO.deleteEntryApproved(entry)
    }

    fun approveEntry(pos:Int){
        unApproveEntriesDAO.entryApprove(pos)
    }
    fun rejectedEntry(entry:Entries){
        unApproveEntriesDAO.entryRejected(entry)
    }

    fun deleteUnApprovedEntryThenUpdateLedgerEntry(pos: Int) {
unApproveEntriesDAO.deleteUnApprovedEntryThenUpdateLedgerEntry(pos)
    }

    fun acceptDeleteEntryRequestFromApprovedEntries_withVoice(entry: Entries) {
        //now check if receiver has voice stored in device..then delete it from there first
        //delete voice from device
        var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)
        // this type of vibration requires API 29
        var path= contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+entry.voiceNote!!.fileName




        val fdelete= File(path)
        if (fdelete.exists()) {
            if (fdelete.delete()) {

                Log.d("unApprovedEntriesRepo", "deleteVoiceFromDevice: Voice Deleted From Device")

                unApproveEntriesDAO.acceptDeleteEntryRequestFromApprovedEntriesWithVoice(entry)
            } else {

                Log.d("unApprovedEntriesRepo", "deleteVoiceFromDevice: Voice Cannot Be Deleted From Device")
            }
        }else{                unApproveEntriesDAO.acceptDeleteEntryRequestFromApprovedEntriesWithVoice(entry)
        }

    }


     suspend fun deleteEntry_EditEntry_withVoice(entry: Entries) {
        //now check if receiver has voice stored in device..then delete it from there first
        //delete voice from device
        var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)
        // this type of vibration requires API 29
        var path= contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+entry.voiceNote!!.fileName




        val fdelete= File(path)
        if (fdelete.exists()) {
            if (fdelete.delete()) {

                Log.d("unApprovedEntriesRepo", "deleteVoiceFromDevice: Voice Deleted From Device")

                unApproveEntriesDAO.deleteEntryEditEntryWithVoice(entry)
            } else {

                Log.d("unApprovedEntriesRepo", "deleteVoiceFromDevice: Voice Cannot Be Deleted From Device")
            }
        }else{                unApproveEntriesDAO.acceptDeleteEntryRequestFromApprovedEntriesWithVoice(entry)
        }

    }

}