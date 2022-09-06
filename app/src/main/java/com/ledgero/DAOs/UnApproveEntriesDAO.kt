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

class UnApproveEntriesDAO(private val ledgerUID: String) {
    private val TAG = "UnApproveEntriesDAO"
    private  var db_reference = FirebaseDatabase.getInstance().reference

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
            .setValue(entry).addOnCompleteListener(){
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
            .addOnCompleteListener(){
                deleteEntryFromUnApproved(entryKey)
            }
    }

    private fun checkAndDeleteFromCanceledEntries(entryKey: String) {
        db_reference.child("canceledEntries").child(ledgerUID).child(entryKey)
            .removeValue()
            .addOnCompleteListener() {
                Log.d(TAG, "checkAndDeleteFromCanceledEntries: Delete From Canceled")
            }

    }
    private fun deleteEntryFromUnApproved(entryKey: String){
        db_reference.child("entriesRequests").child(ledgerUID).child(entryKey)
            .removeValue()
            .addOnCompleteListener(){
                Log.d(TAG, "deleteEntryFromUnApproved: Entery Added in ledger and deleted from UnApproved")

            }
    }

    fun enteryRejected(entry:Entries){
        //add this entry into cancel entries then remove it from here

        var key= entry.entryUID.toString()

        addEntryInRejectEntries(key,entry)
    }
    fun enteryRejected(pos:Int){
        //add this entry into cancel entries then remove it from here
        var entry = unApprovedentriesData.get(pos)
        var key= entry.entryUID.toString()
        addEntryInRejectEntries(key,entry)
    }
    private fun addEntryInRejectEntries(entryKey:String, entry: Entries){

        db_reference.child("canceledEntries").child(ledgerUID).child(entryKey).setValue(entry)
            .addOnCompleteListener(){
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
            .setValue(entry).addOnCompleteListener(){
                if (it.isSuccessful){
                    Log.d(TAG, "updateEntryInLedgerEntries: Entry Updated!!")

                }
            }
    }


}