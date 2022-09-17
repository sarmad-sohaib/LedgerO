package com.ledgero.DAOs

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import org.w3c.dom.EntityReference
import java.io.File

class IndividualScreenDAO(private val ledgerUID: String) {
    private val TAG = "SingleLedgerDAO"
  private  var db_reference = FirebaseDatabase.getInstance().reference
    private var storeage_reference= FirebaseStorage.getInstance().reference

  private  var ledgerEntiresLiveData = MutableLiveData<ArrayList<Entries>>()
    private var entriesData = ArrayList<Entries>()

    private lateinit var ledgerCreatedby:String
    private  var totalAmout:Float =0.0f
    private var totalEntreis:Int=0
    private var giveTakeFlag:Boolean?=null
    private var totaAmountLiveData= MutableLiveData<Float>()
    private var giveTakeFlagLiveData= MutableLiveData<Boolean?>()

    private var unAprrovedEntriesCount= MutableLiveData<Long>()
    private var canceledEntriesCount= MutableLiveData<Long>()



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
        entry.originally_addedByUID= entry.entryMadeBy_userID
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

    private fun addNewEntryWithVoiceRecord(entry: Entries){
        entry.requestMode=1
        db_reference.child("entriesRequests").child(ledgerUID).child(entry.entryUID!!).setValue(entry)
            .addOnCompleteListener()
            {
                if (it.isSuccessful){
                    Log.d(TAG, "addNewEntry: Updated Successfully!!")
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
                db_reference.child("entriesRequests").child(ledgerUID).child(key!!).
                    updateChildren(mapOf("entry_timeStamp" to ServerValue.TIMESTAMP))

                Log.d(TAG, "addNewEntry: Updated Successfully!!")
            }
        }

    }

    fun uploadVoiceNoteThenAddNewEntry(entry: Entries) {
        var file = Uri.fromFile(File(entry.voiceNote!!.localPath))
        var key= db_reference.child("entriesRequests").child(ledgerUID).push().key
        entry.entryUID=key
        var ref= storeage_reference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
          .child("${file.lastPathSegment}")

        ref.putFile(file)
        .addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Voice Note Uploaded. -- ${it.metadata.toString()} ")

            //getDownloadURL
            ref.downloadUrl.addOnCompleteListener(){
                if (it.isSuccessful){
                    entry.voiceNote!!.firebaseDownloadURI=it.result.toString()
                    addNewEntryWithVoiceRecord(entry)
                }
                else{
                    Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Not Able To Fetch Download URI")
                }
            }

        }.addOnFailureListener {
              Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Cannot Upload Voice Note")
          }


    }


    fun getTotalAmount():LiveData<Float>{
        return totaAmountLiveData
    }
    fun getGiveTakeFlag():LiveData<Boolean?>{
        return giveTakeFlagLiveData
    }

    fun getLedgerCreatedBy():String{
        return ledgerCreatedby
    }

    fun getLedgerMetaData(){
        addLedgerMetaListener()
    }


    private fun addLedgerMetaListener() {

        db_reference.child("ledgerInfo").child(ledgerUID).addValueEventListener(metaDataListener)
    }

    fun removeLedgerMetaDataListener(){
        db_reference.child("ledgerInfo").child(ledgerUID).removeEventListener(metaDataListener)

    }

  private  var listenerForUnApproveEntriesDAO =object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.exists()){
            unAprrovedEntriesCount.value=snapshot.childrenCount
        }else{
            unAprrovedEntriesCount.value=0
        }
        }

        override fun onCancelled(error: DatabaseError) {
          ;
        }

    }
    fun startListeningForUnApprovedEntries(): LiveData<Long> {

        db_reference.child("entriesRequests").child(ledgerUID).addValueEventListener(listenerForUnApproveEntriesDAO)

        return unAprrovedEntriesCount

    }

    fun stopListeningForUnApprovedEntries() {
        db_reference.child("entriesRequests").child(ledgerUID).removeEventListener(listenerForUnApproveEntriesDAO)

    }



 private   var listenerForCanceledEntries =object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){

                var count=0L;
                for (i in snapshot.children) {
                    var entry: Entries = i.getValue<Entries>()!!
                    if(entry.entryMadeBy_userID.equals(User.userID)){
                        count++
                    }
                }

                canceledEntriesCount.value=count
            }else{
                canceledEntriesCount.value=0
            }
        }

        override fun onCancelled(error: DatabaseError) {
            ;
        }}


    fun startListeningForCancelledEntries(): LiveData<Long> {

        db_reference.child("canceledEntries").child(ledgerUID).addValueEventListener(listenerForCanceledEntries)

        return canceledEntriesCount

    }

    fun stopListeningForCancelledEntries() {
        db_reference.child("canceledEntries").child(ledgerUID).removeEventListener(listenerForCanceledEntries)

    }









    private var metaDataListener = object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
          if (snapshot.exists()){
             ledgerCreatedby= snapshot.child("ledgerCreatedByUID").value.toString()
              totalAmout= snapshot.child("total_amount").getValue<Float>()!!
              totalEntreis= snapshot.child("total_entries").getValue<Int>()!!
                giveTakeFlag= snapshot.child("give_take_flag").getValue<Boolean?>()
              giveTakeFlagLiveData.value=giveTakeFlag
              totaAmountLiveData.value=totalAmout


          }
        }

        override fun onCancelled(error: DatabaseError) {
            ;
        }

    }

}