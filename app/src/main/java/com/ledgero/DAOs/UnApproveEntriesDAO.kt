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
            }
        }

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


    fun deleteEntry(pos: Int){
        unApprovedentriesData.removeAt(pos)

        db_reference.child("entriesRequests").child(ledgerUID).push().setValue(unApprovedentriesData).addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "DelteEntry: Deleted Successfully!!")
            }
        }
    }


    fun entryApprove(pos: Int){
        ;
    }

}