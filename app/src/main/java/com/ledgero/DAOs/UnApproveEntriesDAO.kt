package com.ledgero.DAOs

import android.content.ContextWrapper
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
import java.io.File

class UnApproveEntriesDAO(private val ledgerUID: String) {
    private val TAG = "UnApproveEntriesDAO"
    private  var db_reference = FirebaseDatabase.getInstance().reference
    private var storage_reference= FirebaseStorage.getInstance().reference
    private  var unApprovedEntiresLiveData = MutableLiveData<ArrayList<Entries>>()
    private var unApprovedentriesData = ArrayList<Entries>()

    private    var listener= object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                unApprovedentriesData= ArrayList<Entries>()

                for (i in snapshot.children){
                    var entry: Entries = i.getValue<Entries>()!!
                    unApprovedentriesData.add(0,entry)//adding  at 0 index so latest entry shows on top

                }
                 unApprovedEntiresLiveData.value=unApprovedentriesData
            }else{
            unApprovedEntiresLiveData.value=ArrayList<Entries>()
        }}

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }


    fun getUnApprovedEntries(): LiveData<ArrayList<Entries>> {

        addListener()

        return unApprovedEntiresLiveData

    }

    fun removeListener(){
        db_reference.child("entriesRequests").child(ledgerUID).removeEventListener(listener)

    }
    fun addListener(){
        db_reference.child("entriesRequests").child(ledgerUID).addValueEventListener(listener)

    }

    fun rejectDeleteRequestForApprovedEntry(pos:Int){
      //  We need to set its reqMode to 0 in ledger list, so another request can be performed on it
        //and save a copy in canceled with reqCode 2, so if requester press requestAgain button it will work
    var entry= unApprovedentriesData.get(pos)

       updateEntryInLedger(entry) //updating in ledgersEntries
    }

    private fun updateEntryInLedger(entry: Entries) {
        entry.requestMode=0
        db_reference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID.toString())
            .setValue(entry).addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "DelteEntry: Deleted Successfully From Ledger!!")
                  entry.requestMode=2
                    enteryRejected(entry)// add this entry in canceled
                }
            }

    }

    fun deleteEntry_Approved(pos: Int){
      //first delete it from ledgers then from requestedEntries
        //then check if its in canceled of any user
        //delete it from there too
        var entry= unApprovedentriesData.get(pos)
        var key= entry.entryUID.toString()
        entry.requestMode=0


        db_reference.child("ledgerEntries").child(ledgerUID).child(key).removeValue().addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "DelteEntry: Deleted Successfully From Ledger!!")
                deleteEntryFromUnApproved(key)
                checkAndDeleteFromCanceledEntries(key)
            }
        }
    }


    fun entryApprove(pos: Int){
        //add entry into ledgers then delet it from request

        var entry = unApprovedentriesData.get(pos)
        entry.isApproved=true
        entry.requestMode=0
        var key= entry.entryUID.toString()
        addEntryInLedger(key,entry)


    }
    private fun addEntryInLedger(entryKey:String, entry: Entries){

        db_reference.child("ledgerEntries").child(ledgerUID).child(entryKey).setValue(entry)
            .addOnCompleteListener {
                deleteEntryFromUnApproved(entryKey)
            }
    }

    private fun checkAndDeleteFromCanceledEntries(entryKey: String) {
        db_reference.child("canceledEntries").child(ledgerUID).child(entryKey)
            .removeValue()
            .addOnCompleteListener {
                Log.d(TAG, "checkAndDeleteFromCanceledEntries: Delete From Canceled")
            }

    }
    private fun deleteEntryFromUnApproved(entryKey: String){
        db_reference.child("entriesRequests").child(ledgerUID).child(entryKey)
            .removeValue()
            .addOnCompleteListener {
                Log.d(TAG, "deleteEntryFromUnApproved: Entery Added in ledger and deleted from UnApproved")

            }
    }

    fun enteryRejected(entry:Entries){
        //add this entry into cancel entries then remove it from here

        var key= entry.entryUID.toString()

        addEntryInRejectEntries(key,entry)
    }

    private fun addEntryInRejectEntries(entryKey:String, entry: Entries){

        db_reference.child("canceledEntries").child(ledgerUID).child(entryKey).setValue(entry)
            .addOnCompleteListener {
                deleteEntryFromUnApproved(entryKey)
            }
    }

    fun deleteUnApprovedEntryThenUpdateLedgerEntry(pos: Int){
      var entry= unApprovedentriesData.get(pos)
        entry.requestMode=0
        deleteEntryFromUnApproved(entry.entryUID.toString())
        updateEntryInLedgerEntries(entry)
    }

    private fun updateEntryInLedgerEntries(entry: Entries){
        db_reference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID.toString())
            .setValue(entry).addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "updateEntryInLedgerEntries: Entry Updated!!")

                }
            }
    }

    /*
    this function will be called when requester delete the request from unapproved entries made by him/her to add
    this new entry in ledger, withVoice means this ledger has voice file so we need to delete that file from local
    and online storage
    */
    fun delete_NewEntryAddRequestFromUnApproved_withVoice(entry: Entries) {

        //Steps:
        //1- Delete From FirebaseStorage, if succesfull then move to step 2, otherwise prompt user that cannot delete entry for now
        //2- Check and delete voice from device storage, then move to step 3
        //3- Now, we can safely delete entry from firebase unApprovedEntries

      //step-1
        deleteVoiceFromFirebaseStorage(entry)




    }

    private fun deleteVoiceFromDevice(entry: Entries) {
        //delete voice from device
        var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)

        val fdelete= File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+entry.voiceNote!!.fileName
        )
        if (fdelete.exists()) {
            if (fdelete.delete()) {

                Log.d(TAG, "deleteVoiceFromDevice: Voice Deleted From Device")
                //step 3
                deleteEntryFromUnApproved(entry.entryUID.toString())
            } else {

                Log.d(TAG, "deleteVoiceFromDevice: Voice Cannot Be Deleted From Device")
             }
        }
    }
    private fun deleteVoiceFromFirebaseStorage(entry: Entries) {

        var file = Uri.fromFile(File(entry.voiceNote!!.localPath))
        storage_reference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
            .child("${file.lastPathSegment}").delete().addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")
                    //step 2
                    deleteVoiceFromDevice(entry)
                }else{
                    Log.d(TAG, "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                }
            }

    }

    fun acceptDeleteEntryRequestFromApprovedEntries_withVoice(entry:Entries) {
        var file = Uri.fromFile(File(entry.voiceNote!!.localPath))
        storage_reference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
            .child("${file.lastPathSegment}").delete().addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")
                   //so now we have deleted voice from device and firestorage too
                    //now we can delete the entry

                    var key= entry.entryUID.toString()
                    db_reference.child("ledgerEntries").child(ledgerUID).child(key).removeValue().addOnCompleteListener()
                    {
                        if (it.isSuccessful){
                            Log.d(TAG, "DelteEntry: Deleted Successfully From Ledger!!")

                            deleteEntryFromUnApproved(key)
                            checkAndDeleteFromCanceledEntries(key)
                        }
                    }
                }else{
                    Log.d(TAG, "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                }
            }

    }


}