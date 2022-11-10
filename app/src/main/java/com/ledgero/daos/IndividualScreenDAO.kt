package com.ledgero.daos

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.other.Constants
import com.ledgero.other.Constants.ADD_REQUEST_REQUEST_MODE
import com.ledgero.pushnotifications.PushNotification
import java.io.File


private const val TAG = "SingleLedgerDAO"

class IndividualScreenDAO(private val ledgerUID: String) {

    private var dbReference = FirebaseDatabase.getInstance().reference
    private var storageReference = FirebaseStorage.getInstance().reference
    private var ledgerEntriesLiveData = MutableLiveData<ArrayList<Entries>>()
    private var entriesData = ArrayList<Entries>()
    private lateinit var ledgerCreatedBy: String
    private var totalAmount: Float = 0.0f
    private var totalEntries: Int = 0
    private var giveTakeFlag: Boolean? = null
    private var totalAmountLiveData = MutableLiveData<Float>()
    private var giveTakeFlagLiveData = MutableLiveData<Boolean?>()
    private var unApprovedEntriesCount = MutableLiveData<Long>()
    private var canceledEntriesCount = MutableLiveData<Long>()
    private var pushNotificationInterface= PushNotification()

    private var listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                entriesData = ArrayList()
                for (i in snapshot.children) {
                    val entry: Entries = i.getValue<Entries>()!!
                    entriesData.add(0, entry)//adding  at 0 index so latest entry shows on top
                     }
                ledgerEntriesLiveData.value = entriesData
            } else { ledgerEntriesLiveData.value = ArrayList() } }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    fun getEntries(): LiveData<ArrayList<Entries>> {
        addListener()
        return ledgerEntriesLiveData
    }

    fun removeListener() {
        dbReference.child("ledgerEntries").child(ledgerUID).removeEventListener(listener)
    }

    private fun addListener() {
        dbReference.child("ledgerEntries").child(ledgerUID).addValueEventListener(listener)
    }


    fun deleteEntry(pos: Int) {
        val key = entriesData[pos].entryUID.toString()
        val entry = entriesData[pos]
        entry.requestMode = 2
        entry.originally_addedByUID = entry.entryMadeBy_userID
        entry.entryMadeBy_userID = User.userID
        dbReference.child("entriesRequests").child(ledgerUID).child(key).setValue(entry)
            .addOnCompleteListener()
            {
                if (it.isSuccessful) {
                    Log.d(TAG, "deleteEntry: Requested To Delete!!")
                    updateEntry(entry)
                    pushNotificationInterface.createAndSendNotification(ledgerUID,
                        Constants.DELETE_REQUEST_REQUEST_MODE)
                }
            } }

    private fun updateEntry(entry: Entries) {
        dbReference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID!!).setValue(entry)
            .addOnCompleteListener()
            {
                if (it.isSuccessful) {
                    Log.d(TAG, "UpdateEntry: Updated Successfully!!")
                }
            }
    }

    private fun addNewEntryWithVoiceRecord(entry: Entries) {
        entry.requestMode = 1
        dbReference.child("entriesRequests").child(ledgerUID).child(entry.entryUID!!)
            .setValue(entry)
            .addOnCompleteListener()
            {
                if (it.isSuccessful) {
                    Log.d(TAG, "addNewEntry: Updated Successfully!!")

                    pushNotificationInterface.createAndSendNotification(ledgerUID, ADD_REQUEST_REQUEST_MODE)
                }
            }
    }

    fun addNewEntry(entry: Entries) {


        val key = dbReference.child("entriesRequests").child(ledgerUID).push().key
        entry.entryUID = key
        entry.requestMode = 1
        dbReference.child("entriesRequests").child(ledgerUID).child(key!!).setValue(entry)
            .addOnCompleteListener()
            {
                if (it.isSuccessful) {
                    dbReference.child("entriesRequests").child(ledgerUID).child(key)
                        .updateChildren(mapOf("entry_timeStamp" to ServerValue.TIMESTAMP))
                    Log.d(TAG, "addNewEntry: Updated Successfully!!")


                    pushNotificationInterface.createAndSendNotification(ledgerUID, ADD_REQUEST_REQUEST_MODE)

                }
            }

    }

    fun uploadVoiceNoteThenAddNewEntry(entry: Entries) {
        val file = Uri.fromFile(entry.voiceNote!!.localPath?.let { File(it) })
        val key = dbReference.child("entriesRequests").child(ledgerUID).push().key
        entry.entryUID = key
        val ref =
            storageReference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
                .child("${file.lastPathSegment}")

        ref.putFile(file)
            .addOnSuccessListener { it ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Log.d(TAG,
                    "uploadVoiceNoteThenAddNewEntry: Voice Note Uploaded. -- ${it.metadata.toString()} ")

                //getDownloadURL
                ref.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        entry.voiceNote!!.firebaseDownloadURI = it.result.toString()
                        addNewEntryWithVoiceRecord(entry)
                    } else {
                        Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Not Able To Fetch Download URI")
                    }
                }

            }.addOnFailureListener {
                Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Cannot Upload Voice Note")
            }


    }


    fun getTotalAmount(): LiveData<Float> {
        return totalAmountLiveData
    }

    fun getGiveTakeFlag(): LiveData<Boolean?> {
        return giveTakeFlagLiveData
    }

    fun getLedgerCreatedBy(): String {
        return ledgerCreatedBy
    }

    fun getLedgerMetaData() {
        addLedgerMetaListener()
    }


    private fun addLedgerMetaListener() {

        dbReference.child("ledgerInfo").child(ledgerUID).addValueEventListener(metaDataListener)
    }

    fun removeLedgerMetaDataListener() {
        dbReference.child("ledgerInfo").child(ledgerUID).removeEventListener(metaDataListener)

    }

    private var listenerForUnApproveEntriesDAO = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                unApprovedEntriesCount.value = snapshot.childrenCount
            } else {
                unApprovedEntriesCount.value = 0
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit

    }

    fun startListeningForUnApprovedEntries(): LiveData<Long> {

        dbReference.child("entriesRequests").child(ledgerUID)
            .addValueEventListener(listenerForUnApproveEntriesDAO)

        return unApprovedEntriesCount

    }

    fun stopListeningForUnApprovedEntries() {
        dbReference.child("entriesRequests").child(ledgerUID)
            .removeEventListener(listenerForUnApproveEntriesDAO)

    }


    private var listenerForCanceledEntries = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {

                var count = 0L
                for (i in snapshot.children) {
                    val entry: Entries = i.getValue<Entries>()!!
                    if (entry.entryMadeBy_userID.equals(User.userID)) {
                        count++
                    }
                }

                canceledEntriesCount.value = count
            } else {
                canceledEntriesCount.value = 0
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }


    fun startListeningForCancelledEntries(): LiveData<Long> {

        dbReference.child("canceledEntries").child(ledgerUID)
            .addValueEventListener(listenerForCanceledEntries)

        return canceledEntriesCount

    }

    fun stopListeningForCancelledEntries() {
        dbReference.child("canceledEntries").child(ledgerUID)
            .removeEventListener(listenerForCanceledEntries)

    }


    private var metaDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                try {

                    ledgerCreatedBy = snapshot.child("ledgerCreatedByUID").value.toString()
                    totalAmount = snapshot.child("total_amount").getValue<Float>()!!
                    totalEntries = snapshot.child("total_entries").getValue<Int>()!!
                    giveTakeFlag = snapshot.child("give_take_flag").getValue<Boolean?>()
                    giveTakeFlagLiveData.value = giveTakeFlag
                    totalAmountLiveData.value = totalAmount

                }catch (e:Exception){
                    Log.d(TAG, "onDataChange: ${e.message}")
                }

            }
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }

}