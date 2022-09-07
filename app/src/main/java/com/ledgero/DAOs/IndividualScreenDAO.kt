package com.ledgero.DAOs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import org.w3c.dom.EntityReference

class IndividualScreenDAO(private val ledgerUID: String) {
    private val TAG = "SingleLedgerDAO"
  private  var db_reference = FirebaseDatabase.getInstance().reference

  private  var ledgerEntiresLiveData = MutableLiveData<ArrayList<Entries>>()
    private var entriesData = ArrayList<Entries>()


private    var listener= object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                entriesData= ArrayList<Entries>()


                for (i in snapshot.children){
                    var entry: Entries = i.getValue<Entries>()!!
                    entriesData.add(0,entry)//adding  at 0 index so latest entry shows on top

                }

                ledgerEntiresLiveData.value=entriesData
            }else{
            ledgerEntiresLiveData.value=ArrayList<Entries>()
            }
            }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }


    fun getEntries(): LiveData<ArrayList<Entries>> {

        addListener()

        return ledgerEntiresLiveData

    }

    fun removeListener(){
        db_reference.child("ledgerEntries").child(ledgerUID).removeEventListener(listener)

    }
    fun addListener(){
        db_reference.child("ledgerEntries").child(ledgerUID).addValueEventListener(listener)

    }


    fun deleteEntry(pos: Int){

        var key = entriesData.get(pos).entryUID.toString()
        var entry= entriesData.get(pos)
        entry.requestMode=2
        entry.entryMadeBy_userID=User.userID


        db_reference.child("entriesRequests").child(ledgerUID).child(key).setValue(entry).addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "deleteEntry: Requested To Delete!!")
                updateEntry(entry)
            }
        }
    }

    private fun updateEntry(entry: Entries) {
        db_reference.child("ledgerEntries").child(ledgerUID).child(entry.entryUID!!).setValue(entry)
            .addOnCompleteListener()
            {
                if (it.isSuccessful) {
                    Log.d(TAG, "UpdateEntry: Updated Successfully!!")
                }
            }
    }

    fun addNewEntry(entry: Entries) {


        var key= db_reference.child("entriesRequests").child(ledgerUID).push().key
        entry.entryUID=key
        entry.requestMode=1
        db_reference.child("entriesRequests").child(ledgerUID).child(key!!).setValue(entry)
            .addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "addNewEntry: Updated Successfully!!")
            }
        }

    }
}