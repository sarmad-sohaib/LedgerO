package com.ledgero.Repositories

import com.ledgero.DAOs.EditSingleEntriesDAO
import com.ledgero.DataClasses.Entries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class EditSingleEntriesRepo(private val editSingleEntriesDAO: EditSingleEntriesDAO) {


    fun  addEntryInUnApproved_ForEdit(entry:Entries){
        editSingleEntriesDAO.addEntryInUnApproved_ForEdit(entry)
    }

   suspend fun sendEntryForApproval(entry: Entries, file: File){

                var auidoDownloadURL=editSingleEntriesDAO.uploadAudioToStorage(file,entry.entryUID!!)
                entry.voiceNote!!.firebaseDownloadURI=auidoDownloadURL
                editSingleEntriesDAO.addEditEntryToUnApproved(entry)



}
    suspend fun sendEntryForApproval_audioNotUpdated(entry: Entries){

       editSingleEntriesDAO.addEditEntryToUnApproved(entry)



    }
    suspend fun updateCurrentEntryRequestModeInApprovedEntries(entry: Entries,requestMode:Int){

       editSingleEntriesDAO.updateEntryRequestModeInApproved(entry,requestMode)



    }

}