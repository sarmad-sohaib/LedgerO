package com.ledgero.DAOs

import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.other.Constants.EDIT_REQUEST_REQUEST_MODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class EditSingleEntriesDAO(val ledgerUID:String) {
    private val TAG = "EntriesDAO"
    private var db_reference = FirebaseDatabase.getInstance().reference
    private var storage_reference= FirebaseStorage.getInstance().reference


    fun addEntryInUnApproved_ForEdit(entry:Entries){
        entry.requestMode=EDIT_REQUEST_REQUEST_MODE; //requesting for edit the entry

        db_reference.child("entriesRequests").child(ledgerUID).child(entry.entryUID!!)
            .setValue(entry).addOnCompleteListener(){
                Log.d(TAG, "addEntryInUnApproved_ForEdit: Requested To Edit Entry")

            }

    }

    suspend fun uploadAudioToStorage(file: File, entryUID:String):String{
        var file = Uri.fromFile(file)
        var key= entryUID
        var ref= storage_reference.child("temp").child("voiceNotes").child(ledgerUID).child(key)
            .child("${file.lastPathSegment}")

       ref.putFile(file).await()
        var downloadURL= ref.downloadUrl.await()
        return downloadURL.toString()



    }

    suspend fun addEditEntryToUnApproved(entry:Entries){

        var isUpdated=false
        entry.requestMode=EDIT_REQUEST_REQUEST_MODE; //requesting for edit the entry

        var response=db_reference.child("entriesRequests").child(ledgerUID).child(entry.entryUID!!)
            .setValue(entry).addOnCompleteListener(){
                if (it.isSuccessful){
                    Log.d(TAG, "addEditEntryToUnApproved: Entry Sent For Edit Approval Successfully")
                    isUpdated=true

                }else{
                    Log.d(TAG, "addEditEntryToUnApproved: Could not Sent Entry For Edit Approval --Error ${it.exception}")

                }

            }.await()




    }

    //this will update the entry flag in approved list so this entry does not go in edit for 2nd time untill first request is complete
    suspend fun updateEntryRequestModeInApproved(entry: Entries, requestMode:Int)
    {
        db_reference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID!!).child("requestMode").setValue(requestMode).await()

    }

}