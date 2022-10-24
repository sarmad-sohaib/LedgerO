package com.ledgero.UtillClasses

import android.content.ContextWrapper
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG
import com.ledgero.other.Constants.NO_REQUEST_REQUEST_MODE
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.math.log

open class Utill_LedgerCalculationsAfterEdit(var ledgerUID:String,oldEntry: Entries,newEntry: Entries ) {

    protected var db_reference = FirebaseDatabase.getInstance().reference
    protected var storage_reference= FirebaseStorage.getInstance().reference


    protected lateinit var oldEntry: Entries
    protected lateinit var newEntry: Entries
    protected val TAG = "Utill_LedgerMetaData"
init {
    this.oldEntry=oldEntry
    this.newEntry=newEntry
}



   protected suspend fun fetchCurrentMetaDataFromFireBase(): DataSnapshot? {

       Log.d(TAG, "fetchCurrentMetaDataFromFireBase: called")
        var data=       db_reference.child("ledgerInfo").child(ledgerUID).get()
        return data.await()
    }
    protected suspend fun updateMetaDataInFireBase(updateData: Map<String, Any>?): Boolean {

        Log.d(TAG, "updateMetaDataInFireBase: called")
        if (updateData==null) return false
        var isUpdated= CoroutineScope(Dispatchers.IO).async {db_reference.child("ledgerInfo").child(ledgerUID).updateChildren(updateData)}
        return isUpdated.await().isSuccessful

    }

}

   open  class DeleteEntry_EditEntry(ledgerUID: String,oldEntry: Entries,newEntry: Entries):Utill_LedgerCalculationsAfterEdit(ledgerUID,oldEntry,newEntry){


    open suspend fun deleteEntry(oldEntry: Entries):Boolean {


            Log.d(TAG, "deleteEntry: Called")
          var isDeleted= CoroutineScope(Dispatchers.Default).async{


              var current_Data =      fetchCurrentMetaDataFromFireBase()
              var update_Data=   calculateMetaDataAfterDeletingEntryData(current_Data,oldEntry)
              var isMetaDataUpdated=    updateMetaDataInFireBase(update_Data)
              var entryDeleted=  deleteOldEntryFromApprovedEntries(oldEntry)



              Log.d(TAG, "deleteEntry: $entryDeleted")
              true

            }

       return  isDeleted.await()
        }

        private suspend fun deleteOldEntryFromApprovedEntries(entryToBeDeleted: Entries): Boolean {
            Log.d(TAG, "deleteOldEntryFromApprovedEntries: called")

                //first delete it from ledgers then from requestedEntries
                //then check if its in canceled of any user
                //delete it from there too
                var entry= entryToBeDeleted
                var key= entry.entryUID.toString()
                entry.requestMode=0

            var isEntryDeleted=    CoroutineScope(Dispatchers.IO).async {

                   var deleteFromApproved= async {   db_reference.child("ledgerEntries").child(ledgerUID).child(key).removeValue()}
                    if (deleteFromApproved.await().isSuccessful) {
                        checkAndDeleteFromCanceledEntries(key)
                        true
                    }else{
                         Log.d(TAG, "deleteOldEntryFromApprovedEntries: Could Not Delete Entry From Approved Entries")
                    false
                    }
                }


        return isEntryDeleted.await()
        }
        private  fun checkAndDeleteFromCanceledEntries(entryKey:String){

            db_reference.child("canceledEntries").child(ledgerUID).child(entryKey)
                .removeValue()
                .addOnCompleteListener() {
                    Log.d(TAG, "checkAndDeleteFromCanceledEntries: Delete From Canceled")
                }
        }



        private  fun calculateMetaDataAfterDeletingEntryData(currentData: DataSnapshot?, oldEntry: Entries):Map<String,Any>? {

            Log.d(TAG, "calculateMetaDataAfterDeletingEntryData: called")
if (currentData == null){
    return null
}


            var it= currentData
            var totalAmout = it!!.child("total_amount").getValue<Float>()!!
            var totalEntreis = it!!.child("total_entries").getValue<Int>()!!
            var giveTakeFlag = it!!.child("give_take_flag").getValue<Boolean?>()
            var ledgerCreatedBy = it!!.child("ledgerCreatedByUID").getValue<String?>()

            var entrymadebyUserid= oldEntry.originally_addedByUID

            if (entrymadebyUserid.equals(ledgerCreatedBy)){
               return calculateAmountAfterDeleting_createdByOwner(totalAmout,giveTakeFlag,totalEntreis,entrymadebyUserid,ledgerCreatedBy)
            }else{
              return calculateAmountAfterDeleting_createdByNonOwner(totalAmout,giveTakeFlag,totalEntreis,entrymadebyUserid,ledgerCreatedBy)

            }

        }

        //GET_ENTRY_FLAG -> FALSE
        //GAVE_ENTRY_FLAG -> TRUE
           private fun calculateAmountAfterDeleting_createdByOwner(totalAmount: Float, giveTakeFlag: Boolean?, totalEntreis: Int,
                                                                   entrymadebyUserid: String?, ledgerCreatedBy: String?
                                                                    ): HashMap<String, Any>? {

            //1 -- if entry to be deleted have flag = GAVE_ENTRY_FLAG/true , it means owner gave/lend (You'll Get) money
            //now we need to delete this so we will reverse this action
            //so if ledger flag = true we will minus it from total and if amount gets negative we set ledger flag to GET_ENTRY_FLAG/false

            var map = HashMap<String, Any>()

            if(oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
                var newAmount= totalAmount-oldEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag= GET_ENTRY_FLAG
                    newAmount= newAmount * (-1)
                }
                var newEntries = totalEntreis-1

                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map


            }


            //2        // if entry flag=true and ledger flag= false we will add it in total
            if (oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
                var newAmount= totalAmount+oldEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries = totalEntreis-1


                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }


            //3  -- if entry to be delete have flag =false, it means owner got/owe (You'll Give) money
            //so if ledger flag = true we will add it in total

            if (oldEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
                var newAmount= totalAmount+oldEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries = totalEntreis-1
                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }

            //4 -- if entry flag = false and ledger flag false then we will minus it from total and if amounts get negative we set ledger to true
            if(oldEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
                var newAmount= totalAmount-oldEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag= GAVE_ENTRY_FLAG
                    newAmount= newAmount * (-1)
                }
                var newEntries = totalEntreis-1
                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }

            return null

        }

        private fun calculateAmountAfterDeleting_createdByNonOwner(totalAmount: Float, giveTakeFlag: Boolean?, totalEntreis: Int,
                                                                   entrymadebyUserid: String?, ledgerCreatedBy: String?
                                                                    ):HashMap<String,Any>? {

            //1-- if entry is not made by ledger owner and entry flag is true, means he gave/lent (You'll get) money
            //but now we want to delete it so reverse this entry effect
            //so if entryFlag = true && LedgerFlag= true  ==add in total
            //it means we will add in total amount..because ledger is true means owner will get some money..and now we are deleting the
            //the entry which says nonowner gave some money, it means now owner will get this money too

           var map = HashMap<String, Any>()
            if (oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){

                var newAmount= totalAmount+oldEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries = totalEntreis-1

                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }


            //2 --  if entryFlag = true && LedgerFlag = false ==minus from total
            //it means nonowner says delete the amount which I gave..it means now owner owes him less money. So after minus check if amount is
            //is negative then set LedgerFlag = true

            if(oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
                var newAmount= totalAmount-oldEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag= GAVE_ENTRY_FLAG
                    newAmount= newAmount * (-1)
                }
                var newEntries = totalEntreis-1
                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }


            // 3  -- if entryFlag = false && LedgerFlag = true == minus from total
            // it means nonowner got/owe some for this entry but now we delete the entry and now owner will get less money
            //so after minus we need to check and if amount becomes negative we set LedgerFlag = false

            if(oldEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
                var newAmount= totalAmount-oldEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag= GET_ENTRY_FLAG
                    newAmount= newAmount * (-1)
                }
                var newEntries = totalEntreis-1
                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map

            }



            // 4 -- if entryFlag = false && LedgerFlag = false  == add in total
            // it means nonowner got some money but now we says delete this entry so owner has to give this money too


            if (oldEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){

                var newAmount= totalAmount+oldEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries = totalEntreis-1
                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map

            }

            return null
        }






    }

   open  class  AddEntry_EditEntry(ledgerUID: String,oldEntry: Entries,newEntry: Entries):Utill_LedgerCalculationsAfterEdit(ledgerUID,oldEntry,newEntry){

     open   suspend fun addEditedEntryAsNewEntry(newEntry:Entries):Boolean{
            Log.d(TAG, "addEditedEntryAsNewEntry: called")

            var isUpdated= CoroutineScope(Dispatchers.Default).async {

            var current_Data =  fetchCurrentMetaDataFromFireBase()

            var update_Data=   calculateMetaDataAfterAddingEntryData(current_Data,newEntry)

            var isMetaDataUpdated=   updateMetaDataInFireBase(update_Data)
            var updated=   addEditEntryInApprovedEntries(newEntry)



                updated
            }

            return isUpdated.await()
            }

        private suspend fun addEditEntryInApprovedEntries(newEntry: Entries): Boolean {

            Log.d(TAG, "addEditEntryInApprovedEntries: called")
              var entryKey= newEntry.entryUID!!
            newEntry.requestMode= NO_REQUEST_REQUEST_MODE
           var result= CoroutineScope(Dispatchers.IO).async {db_reference.child("ledgerEntries").child(ledgerUID).child(entryKey).setValue(newEntry) }
            result.await()
           deleteEntryFromUnApproved(entryKey)

            return result.await().isSuccessful

        }

        private suspend fun deleteEntryFromUnApproved(entryKey: String){
            db_reference.child("entriesRequests").child(ledgerUID).child(entryKey)
                .removeValue().await()
        }
        private fun calculateMetaDataAfterAddingEntryData(currentData: DataSnapshot?, newEntry: Entries): Map<String,Any>? {

            Log.d(TAG, "calculateMetaDataAfterAddingEntryData: called")
            if (currentData==null){
                Log.d(TAG, "calculateMetaDataAfterAddingEntryData: null--Ending")
                return null
            }

            var it= currentData
            var totalAmout = it!!.child("total_amount").getValue<Float>()!!
            var totalEntreis = it!!.child("total_entries").getValue<Int>()!!
            var giveTakeFlag = it!!.child("give_take_flag").getValue<Boolean?>()
            var ledgerCreatedBy = it!!.child("ledgerCreatedByUID").getValue<String?>()

            var entrymadebyUserid= newEntry.originally_addedByUID
            //check if entry is made by the user who created ledger or not


            //this entry is made by the user who created ledger
            if (entrymadebyUserid!!.equals(ledgerCreatedBy)) {

                Log.d(TAG, "calculateMetaDataAfterAddingEntryData:EntryMade By Owner")
               return calculateAmount_entryCreatedByOwner(totalAmout, giveTakeFlag, totalEntreis)
            } else {

                Log.d(TAG, "calculateMetaDataAfterAddingEntryData:EntryMade By NonOwner")
                return calculateAmount_entryCreatedByNonOwner(totalAmout, giveTakeFlag, totalEntreis)

            }

        }
        private fun calculateAmount_entryCreatedByOwner(
            totalAmout: Float,
            giveTakeFlag: Boolean?,
            totalEntreis: Int
        ):HashMap<String,Any>?
        {
            Log.d(TAG, "calculateAmount_entryCreatedByOwner: called")
            
            var currentEntry=newEntry
            var map = HashMap<String, Any>()
            // entry is being made by the owner of ledger


            //1 --  if -entry flag is GET_ENTRY_FLAG/false it means he got the money so if $ledger flag is GET_ENTRY_FLAG/false we will add this amount
            //in total because it means he is already in debt or he owe(You'll Give)

            if (currentEntry.give_take_flag == GET_ENTRY_FLAG && giveTakeFlag == GET_ENTRY_FLAG) {
                Log.d(TAG, "calculateAmount_entryCreatedByOwner: 1")
                var newAmount = totalAmout + currentEntry.amount!!
                var newTotalEntries = totalEntreis + 1

                map.put("total_amount", newAmount)
                map.put("total_entries", newTotalEntries)
                map.put("give_take_flag", giveTakeFlag)

                return map
            }


            //2 --  if  -entry flag is GET_ENTRY_FLAG/false it means he got money so if $ledger flag is GAVE_ENTRY_FLAG/true we will minus this amount from
            //total because he had lended/Gave(You'll Get) money and now he received some..so we will minus it from total
            //money he gave -- And after minus if amount become negative then we will set the $ledger flag to GET_ENTRY_FLAG/false and if
            //amount becomes 0 we will set the $ledger flag to null means all clear 00 ,00
            if (currentEntry.give_take_flag == GET_ENTRY_FLAG && giveTakeFlag == GAVE_ENTRY_FLAG) {
                Log.d(TAG, "calculateAmount_entryCreatedByOwner: 2")
                var newAmount = totalAmout - currentEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag=GET_ENTRY_FLAG
                    newAmount= newAmount * (-1)
                }


                var newTotalEntries = totalEntreis + 1

                map.put("total_amount", newAmount)
                map.put("total_entries", newTotalEntries)
                map.put("give_take_flag", newFlag)

                return map
            }

            //3 -- if -entry flag is GAVE_ENTRY_FLAG/true it means he gave the money so if $ledger flag is GAVE_ENTRY_FLAG/true we will add this amount in total
            //because he already have lended/Gave(You'll Get) money and how gave some more. So, we will add in total
            if (currentEntry.give_take_flag == GAVE_ENTRY_FLAG && giveTakeFlag == GAVE_ENTRY_FLAG) {
                Log.d(TAG, "calculateAmount_entryCreatedByOwner: 3")
                var newAmount = totalAmout + currentEntry.amount!!
                var newFlag= giveTakeFlag
                var newTotalEntries = totalEntreis + 1

                map.put("total_amount", newAmount)
                map.put("total_entries", newTotalEntries)
                map.put("give_take_flag", newFlag)

                return map
            }



            //4 -- if -entry flag is GAVE_ENTRY_FLAG/true it means he gave the money so if $ledger flag is GET_ENTRY_FLAG/false then we will minus it from total
            //because he lended/gave some money but he owed/get (You'll Give) money..So, now he have paid money we will minus
            //-- And after minus if amount become negative then we will set the $ledger flag to GAVE_ENTRY_FLAG/true because now he has surplus his debt
            // and he will get some money now and if amount becomes 0 we will set the $ledger flag to null means all clear 00 ,00

            if (currentEntry.give_take_flag == GAVE_ENTRY_FLAG && giveTakeFlag == GET_ENTRY_FLAG) {
                Log.d(TAG, "calculateAmount_entryCreatedByOwner: 4")
                var newAmount = totalAmout - currentEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){newFlag=GAVE_ENTRY_FLAG
                    newAmount=newAmount* (-1)
                }

                var newTotalEntries = totalEntreis + 1

                map.put("total_amount", newAmount)
                map.put("total_entries", newTotalEntries)
                map.put("give_take_flag", newFlag)

                return map
            }
            Log.d(TAG, "calculateAmount_entryCreatedByOwner: ended")

       return null
        }

        private fun calculateAmount_entryCreatedByNonOwner(
            totalAmout: Float,
            giveTakeFlag: Boolean?,
            totalEntreis: Int
        ): HashMap<String, Any>? {

            Log.d(TAG, "calculateAmount_entryCreatedByNonOwner: called")
            
            var currentEntry=newEntry
            var map = HashMap<String, Any>()
            //1 -- if -entry flag is GET_ENTRY_FLAG/false it means nonowner got(You'll Gave) the money and if $ledger flag is GET_ENTRY_FLAG/false it means owner owes(You'll Give)
            // so we will minus the amount from the total because owner owes the money and nonowner says he got the money..so we
            //will minus it from total -- After minus if amount becomes negative then we will set the $ledger flag to GAVE_ENTRY_FLAG/true and if amount
            //amount becomes 0 then flag to null
            if(currentEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag==GET_ENTRY_FLAG){
                Log.d(TAG, "calculateAmount_entryCreatedByNonOwner: 1")
                var newAmount = totalAmout- currentEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag=true
                    newAmount= newAmount * (-1)

                }

                var newEntries= totalEntreis+1

                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map

            }

            //2 -- if -entry flag is GET_ENTRY_FLAG/false it means nonowner got(You'll Gave) the money and if $ledger flag is GAVE_ENTRY_FLAG/true then it means we will add the
            //amount in total because

            if(currentEntry.give_take_flag==GET_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
                Log.d(TAG, "calculateAmount_entryCreatedByNonOwner: 2")
                var newAmount = totalAmout+ currentEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries= totalEntreis+1

                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }

            //3 -- if -entry flag is GAVE_ENTRY_FLAG/true it means nonowner gave(You'll Get) the money and if $ledger flag is GET_ENTRY_FLAG/false it means owner owes(You'll Give) the
            //money and nononwer says he has gave more money...so we will add it in total
            if(currentEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
                Log.d(TAG, "calculateAmount_entryCreatedByNonOwner: 3")
                var newAmount = totalAmout+ currentEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries= totalEntreis+1

                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map
            }



            //4 -- if -entry flag is GAVE_ENTRY_FLAG/true it means nonowner gave(You'll Get) the money and if $ledger flag is GAVE_ENTRY_FLAG/true lend/gave(You'll Get) the money
            //so now nononwer says he gave some money, so we will minus it from total-- After minus if amount becomes negative $ledger flag
            //will be set to GET_ENTRY_FLAG/false because now owner owes some money and if amount becomes 0 $ledger flag set to null

            if(currentEntry.give_take_flag==GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
                Log.d(TAG, "calculateAmount_entryCreatedByNonOwner: 4")
                var newAmount = totalAmout- currentEntry.amount!!
                var newFlag= giveTakeFlag
                if (newAmount<0){
                    newFlag=GET_ENTRY_FLAG
                    newAmount= newAmount * (-1)

                }

                var newEntries= totalEntreis+1

                map.put("total_amount", newAmount)
                map.put("total_entries", newEntries)
                map.put("give_take_flag", newFlag)

                return map

            }



            return null
        }

    }

    open class DeleteEntryWithVoice_EditEntry(ledgerUID: String,oldEntry: Entries,newEntry: Entries):DeleteEntry_EditEntry(ledgerUID, oldEntry, newEntry)
    {

        override suspend fun deleteEntry(oldEntry: Entries): Boolean {
                deleteVoiceFromFirebaseStorage(oldEntry)
                deleteVoiceFromLocalDevice(oldEntry)

            return super.deleteEntry(oldEntry)
        }



           private fun deleteVoiceFromLocalDevice(oldentry: Entries){
            //delete voice from device

              CoroutineScope(Dispatchers.IO).launch {
                  var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)


                  val fdelete= File( contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+oldentry.voiceNote!!.fileName
                  )
                  if (fdelete.exists()) {
                      if (fdelete.delete()) {

                          Log.d(TAG, "deleteVoiceFromDevice: Voice Deleted From Device")

                      } else {

                          Log.d(TAG, "deleteVoiceFromDevice: Voice Cannot Be Deleted From Device")
                      }
                  }
              }

         }

        suspend private fun  deleteVoiceFromFirebaseStorage(entry: Entries) {

            var file = Uri.fromFile(File(entry.voiceNote!!.localPath))
            storage_reference.child("voiceNotes").child(ledgerUID).child(entry.entryUID.toString())
                .child("${file.lastPathSegment}").delete().addOnCompleteListener {
                    if (it.isSuccessful){
                        Log.d(TAG, "deleteVoiceFromFirebaseStorage: Voice Deleted From Firebase Storage")

                    }else{
                        Log.d(TAG, "deleteVoiceFromFirebaseStorage: Cannot Delete Voice From Firebase Storage. ${it.exception.toString()}")
                    }
                }.await()

        }




    }

    open class AddEntryWithVoice_EditEntry(ledgerUID: String,oldEntry: Entries,newEntry: Entries):AddEntry_EditEntry(ledgerUID, oldEntry, newEntry)
    {

        override suspend fun addEditedEntryAsNewEntry(newEntry: Entries): Boolean {
            if (newEntry.hasVoiceNote!!){
                uploadVoiceNoteThenAddNewEntry(newEntry)
            }

            return super.addEditedEntryAsNewEntry(newEntry)
        }

       private suspend fun uploadVoiceNoteThenAddNewEntry(newEntry: Entries) {
            var file = Uri.fromFile(File(newEntry.voiceNote!!.localPath))

            var ref= storage_reference.child("voiceNotes").child(ledgerUID).child(newEntry.entryUID.toString())
                .child("${file.lastPathSegment}")

            ref.putFile(file)
                .addOnSuccessListener {
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Voice Note Uploaded. -- ${it.metadata.toString()} ")

                    //getDownloadURL
                    ref.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful){
                            newEntry.voiceNote!!.firebaseDownloadURI=it.result.toString()

                        }
                        else{
                            Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Not Able To Fetch Download URI")
                        }
                    }

                }.addOnFailureListener {
                    Log.d(TAG, "uploadVoiceNoteThenAddNewEntry: Cannot Upload Voice Note")
                }.await()


        }



    }





