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

                entriesData= snapshot.getValue<ArrayList<Entries>>()!!


                ledgerEntiresLiveData.value=entriesData
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
        entriesData.removeAt(pos)

        db_reference.child("ledgerEntries").child(ledgerUID).setValue(entriesData).addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "addNewEntry: Updated Successfully!!")
            }
        }
    }

    fun addNewEntry(entry: Entries) {

        var key= db_reference.child("entriesRequests").child(ledgerUID).push().key
        db_reference.child("entriesRequests").child(ledgerUID).child(key!!).setValue(entry)
            .addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "addNewEntry: Updated Successfully!!")
            }
        }

    }
}