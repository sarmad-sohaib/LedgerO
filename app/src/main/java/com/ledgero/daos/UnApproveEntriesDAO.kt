package com.ledgero.daos

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
import com.ledgero.other.Constants.ENTRY_REJECTED
import com.ledgero.other.Constants.NO_REQUEST_REQUEST_MODE
import com.ledgero.pushnotifications.PushNotification
import kotlinx.coroutines.tasks.await

import java.io.File

private const val TAG = "UnApproveEntriesDAO"

class UnApproveEntriesDAO(private val ledgerUID: String) {

    private var dbReference = FirebaseDatabase.getInstance().reference
    private var storageReference = FirebaseStorage.getInstance().reference
    private var unApprovedEntriesLiveData = MutableLiveData<ArrayList<Entries>>()
    private var unApprovedEntriesData = ArrayList<Entries>()
    private val pushNotificationInterface= PushNotification()


    private var listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                unApprovedEntriesData = ArrayList()

                for (i in snapshot.children) {
                    val entry: Entries = i.getValue<Entries>()!!
                    unApprovedEntriesData.add(0,
                        entry)//adding  at 0 index so latest entry shows on top

                }
                unApprovedEntriesLiveData.value = unApprovedEntriesData
            } else {
                unApprovedEntriesLiveData.value = ArrayList()
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit

    }


    fun getUnApprovedEntries(): LiveData<ArrayList<Entries>> {

        addListener()

        return unApprovedEntriesLiveData

    }

    fun removeListener() {
        dbReference.child("entriesRequests").child(ledgerUID).removeEventListener(listener)

    }

    private fun addListener() {
        dbReference.child("entriesRequests").child(ledgerUID).addValueEventListener(listener)

    }

    fun rejectDeleteRequestForApprovedEntry(pos: Int) {
        //  We need to set its reqMode to 0 in ledger list, so another request can be performed on it
        //and save a copy in canceled with reqCode 2, so if requester press requestAgain button it will work
        val entry = unApprovedEntriesData[pos]

        updateEntryInLedger(entry) //updating in ledgersEntries
    }

    private fun updateEntryInLedger(entry: Entries) {
        entry.requestMode = 0
        dbReference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID.toString())
            .setValue(entry).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "DeleteEntry: Deleted Successfully From Ledger!!")
                    entry.requestMode = 2
                    entryRejected(entry)// add this entry in canceled
                }
            }

    }

    fun deleteEntryApproved(pos: Int) {
        //first delete it from ledgers then from requestedEntries
        //then check if its in canceled of any user
        //delete it from there too
        val entry = unApprovedEntriesData[pos]
        val key = entry.entryUID.toString()
        entry.requestMode = 0


        dbReference.child("ledgerEntries").child(ledgerUID).child(key).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "DeleteEntry: Deleted Successfully From Ledger!!")
                    deleteEntryFromUnApproved(key)
                    checkAndDeleteFromCanceledEntries(key)
                }
            }
    }

    fun deleteEntryApproved(entryToBeDeleted: Entries) {
        //first delete it from ledgers then from requestedEntries
        //then check if its in canceled of any user
        //delete it from there too
        val key = entryToBeDeleted.entryUID.toString()
        entryToBeDeleted.requestMode = 0


        dbReference.child("ledgerEntries").child(ledgerUID).child(key).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "DeleteEntry: Deleted Successfully From Ledger!!")
                    checkAndDeleteFromCanceledEntries(key)
                }
            }
    }


    fun entryApprove(pos: Int) {
        //add entry into ledgers then delete it from request

        val entry = unApprovedEntriesData[pos]
        entry.isApproved = true
        entry.requestMode = NO_REQUEST_REQUEST_MODE
        val key = entry.entryUID.toString()
        addEntryInLedger(key, entry)


    }

    private fun addEntryInLedger(entryKey: String, entry: Entries) {

        dbReference.child("ledgerEntries").child(ledgerUID).child(entryKey).setValue(entry)
            .addOnCompleteListener {
                deleteEntryFromUnApproved(entryKey)
            }
    }

    private fun checkAndDeleteFromCanceledEntries(entryKey: String) {
        dbReference.child("canceledEntries").child(ledgerUID).child(entryKey).removeValue()
            .addOnCompleteListener {
                Log.d(TAG, "checkAndDeleteFromCanceledEntries: Delete From Canceled")
            }

    }

    private fun deleteEntryFromUnApproved(entryKey: String) {
        dbReference.child("entriesRequests").child(ledgerUID).child(entryKey).removeValue()
            .addOnCompleteListener {

                Log.d(TAG,
                    "deleteEntryFromUnApproved: Entry Added in ledger and deleted from UnApproved")

            }
    }

    fun entryRejected(entry: Entries) {
        //add this entry into cancel entries then remove it from here

        val key = entry.entryUID.toString()

        addEntryInRejectEntries(key, entry)
    }

    private fun addEntryInRejectEntries(entryKey: String, entry: Entries) {

        dbReference.child("canceledEntries").child(ledgerUID).child(entryKey).setValue(entry)
            .addOnCompleteListener {
                deleteEntryFromUnApproved(entryKey)
            }
    }


    fun deleteUnApprovedEntryThenUpdateLedgerEntry(pos: Int) {
        val entry = unApprovedEntriesData[pos]
        entry.requestMode = 0
        deleteEntryFromUnApproved(entry.entryUID.toString())
        updateEntryInLedgerEntries(entry)
    }

    private fun updateEntryInLedgerEntries(entry: Entries) {
        dbReference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID.toString())
            .setValue(entry).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "updateEntryInLedgerEntries: Entry Updated!!")

                }
            }
    }

    /*
    this function will be called when requester delete the request from unapproved entries made by him/her to add
    this new entry in ledger, withVoice means this ledger has voice file so we need to delete that file from local
    and online storage
    */
    fun deleteNewEntryAddRequestFromUnApprovedWithVoice(entry: Entries) {

        //Steps:
        //1- Delete From FirebaseStorage, if successful then move to step 2, otherwise prompt user that cannot delete entry for now
        //2- Check and delete voice from device storage, then move to step 3
        //3- Now, we can safely delete entry from firebase unApprovedEntries

        //step-1
        deleteVoiceFromFirebaseStorage(entry)


    }

    private fun deleteVoiceFromDevice(entry: Entries) {
        //delete voice from device
        val contextWrapper =
            ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)

        val fileDelete = File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
            .toString() + "/" + entry.voiceNote!!.fileName)
        if (fileDelete.exists()) {
            if (fileDelete.delete()) {

                Log.d(TAG, "deleteVoiceFromDevice: Voice Deleted From Device")
                //step 3
                deleteEntryFromUnApproved(entry.entryUID.toString())
            } else {

                Log.d(TAG, "deleteVoiceFromDevice: Voice Cannot Be Deleted From Device")
            }
        }
    }

    private fun deleteVoiceFromFirebaseStorage(entry: Entries) {

        val file = Uri.fromFile(entry.voiceNote!!.localPath?.let { File(it) })
        storageReference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
            .child("${file.lastPathSegment}").delete().addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG,
                        "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")
                    //step 2
                    deleteVoiceFromDevice(entry)
                } else {
                    Log.d(TAG,
                        "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                }
            }

    }

    fun acceptDeleteEntryRequestFromApprovedEntriesWithVoice(entry: Entries) {
        val file = Uri.fromFile(entry.voiceNote!!.localPath?.let { File(it) })
        storageReference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
            .child("${file.lastPathSegment}").delete().addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    Log.d(TAG,
                        "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")
                    //so now we have deleted voice from device and fireStorage too
                    //now we can delete the entry

                    val key = entry.entryUID.toString()
                    dbReference.child("ledgerEntries").child(ledgerUID).child(key).removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(TAG, "DeleteEntry: Deleted Successfully From Ledger!!")

                                deleteEntryFromUnApproved(key)
                                checkAndDeleteFromCanceledEntries(key)
                            }
                        }
                } else {
                    Log.d(TAG,
                        "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                }
            }

    }

    suspend fun deleteEntryEditEntryWithVoice(entry: Entries) {
        val file = Uri.fromFile(entry.voiceNote!!.localPath?.let { File(it) })

        var key = ""
        storageReference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
            .child("${file.lastPathSegment}").delete().addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG,
                        "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")
                    //so now we have deleted voice from device and fireStorage too
                    //now we can delete the entry

                    key = entry.entryUID.toString()

                } else {
                    Log.d(TAG,
                        "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                }
            }.await()
        dbReference.child("ledgerEntries").child(ledgerUID).child(key).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "DeleteEntry: Deleted Successfully From Ledger!!")
                }
            }.await()
        checkAndDeleteFromCanceledEntries(key)


    }

}