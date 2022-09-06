package com.ledgero.DAOs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User

class CanceledEntriesDAO(private val ledgerUID: String) {
    private val TAG = "CanceledEntries"
    private var db_reference = FirebaseDatabase.getInstance().reference

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
            .addOnCompleteListener() {
                updateEntryRequestModeInLedger(entryKey,entry)
                deleteEntryFromCanceled(entryKey)
            }
    }

    private fun updateEntryRequestModeInLedger(entryKey: String, entry: Entries) {

        db_reference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID.toString())
            .setValue(entry).addOnCompleteListener(){
                if (it.isSuccessful){
                    Log.d(TAG, "UpdateEntryRequest: Entry Updated in Ledger")
                    deleteEntryFromCanceled(entryKey)

                }
            }
    }

    private fun deleteEntryFromCanceled(entryKey: String) {
        db_reference.child("canceledEntries").child(ledgerUID).child(entryKey)
            .removeValue()
            .addOnCompleteListener() {
                Log.d(TAG,
                    "deleteEntryFromCanceled: Entery deleted from canceled")
            }
    }


    }



