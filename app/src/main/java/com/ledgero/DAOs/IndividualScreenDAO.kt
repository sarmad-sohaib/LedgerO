package com.ledgero.DAOs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import org.w3c.dom.EntityReference

class IndividualScreenDAO(private val ledgerUID: String) {
    private val TAG = "SingleLedgerDAO"
  private  var db_reference = FirebaseDatabase.getInstance().reference

  private  var ledgerEntiresLiveData = MutableLiveData<ArrayList<Entries>>()
    private var entriesData = ArrayList<Entries>()

    init {
//        db_reference.child("ledgerEntries").child(ledgerUID).get().addOnCompleteListener() {
//            if (it.isSuccessful) {
//                if(it.result.exists()){
//                    var data =  it.result.getValue<ArrayList<Entries>>()!!
//                    ledgerEntiresLiveData.value = data
//
//                }else{
//                    ledgerEntiresLiveData.value=entriesData
//                }
//            }
//
//        }

    }

private    var listener= object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                entriesData= ArrayList<Entries>()

                entriesData= snapshot.getValue<ArrayList<Entries>>()!!
//                for(i in snapshot.children){
//
//                    entriesData.add(i.getValue<Entries>()!!)
//                }
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


    fun addNewEntry(entry: Entries) {

ledgerEntiresLiveData.value?.add(0,entry)
        db_reference.child("ledgerEntries").child(ledgerUID).setValue(ledgerEntiresLiveData.value).addOnCompleteListener()
        {
            if (it.isSuccessful){
                Log.d(TAG, "addNewEntry: Updated Successfully!!")
            }
        }

    }
}