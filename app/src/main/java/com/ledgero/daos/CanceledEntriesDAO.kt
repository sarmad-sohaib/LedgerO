package com.ledgero.daos

import android.net.Uri
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
import com.ledgero.DataClasses.User
import com.ledgero.other.Constants
import com.ledgero.pushnotifications.PushNotification
import java.io.File

class CanceledEntriesDAO(private val ledgerUID: String) {
    private val TAG = "CanceledEntries"
    private var db_reference = FirebaseDatabase.getInstance().reference
    private var storage_reference= FirebaseStorage.getInstance().reference

    private val pushNotificationInterface= PushNotification()


    private var canceledEntiresLiveData = MutableLiveData<ArrayList<Entries>>()
    private var canceledEntriesData = ArrayList<Entries>()

    private var listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                canceledEntriesData = ArrayList<Entries>()

                for (i in snapshot.children) {
                    var entry: Entries = i.getValue<Entries>()!!
                if(entry.entryMadeBy_userID.equals(User.userID)){
                    canceledEntriesData.add(0,
                        entry)//adding  at 0 index so latest entry shows on top
                }
                }
                canceledEntiresLiveData.value = canceledEntriesData
            } else {
                canceledEntiresLiveData.value = ArrayList<Entries>()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }


    fun getCanceledEntries(): LiveData<ArrayList<Entries>> {

        addListener()

        return canceledEntiresLiveData

    }

    fun removeListener() {
        db_reference.child("canceledEntries").child(ledgerUID).removeEventListener(listener)

    }

    fun addListener() {
        db_reference.child("canceledEntries").child(ledgerUID).addValueEventListener(listener)

    }


    fun deleteCanceledEntryWithVoice(entry:Entries){
        var key = entry.entryUID.toString()
        //first delete voice note from firebase
        //then delete the entry from canceledEntries
        deleteVoiceFromFirebaseStorage(entry,key)
    }
    private fun deleteVoiceFromFirebaseStorage(entry: Entries,key:String) {
        var file = Uri.fromFile(File(entry.voiceNote!!.localPath))
        storage_reference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
            .child("${file.lastPathSegment}").delete().addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")

                    deleteEntryFromCanceled(key)
                }else{
                    Log.d(TAG, "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                }
            }

    }
    fun deleteCanceledEntry(pos: Int) {
        var entry = canceledEntriesData.get(pos)
        var key = entry.entryUID.toString()

            deleteEntryFromCanceled(key)
    }


    fun requestAgain(pos: Int) {
        //add entry into un-approved then delete it from canceledEntries

        var entry = canceledEntriesData.get(pos)
        entry.isApproved = false
        var key = entry.entryUID.toString()
        addEntryInUnApproved(key, entry)


    }

    private fun addEntryInUnApproved(entryKey: String, entry: Entries) {

        db_reference.child("entriesRequests").child(ledgerUID).child(entryKey).setValue(entry)
            .addOnCompleteListener {
                updateEntryRequestModeInLedger(entryKey,entry)
                deleteEntryFromCanceled(entryKey)
                pushNotificationInterface.createAndSendNotification(ledgerUID,
                    Constants.ENTRY_RESENT)
            }
    }

    private fun updateEntryRequestModeInLedger(entryKey: String, entry: Entries) {

        db_reference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID.toString())
            .setValue(entry).addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "UpdateEntryRequest: Entry Updated in Ledger")
                    deleteEntryFromCanceled(entryKey)

                }
            }
    }

    private fun deleteEntryFromCanceled(entryKey: String) {
        db_reference.child("canceledEntries").child(ledgerUID).child(entryKey)
            .removeValue()
            .addOnCompleteListener {
                Log.d(TAG,
                    "deleteEntryFromCanceled: Entery deleted from canceled")
            }
    }


    }



