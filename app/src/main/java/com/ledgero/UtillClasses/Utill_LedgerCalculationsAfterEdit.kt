package com.ledgero.UtillClasses

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Utill_LedgerCalculationsAfterEdit(var ledgerUID:String ) {

    private var db_reference = FirebaseDatabase.getInstance().reference

    private lateinit var oldEntry: Entries
    private lateinit var newEntry: Entries
    private val TAG = "Utill_LedgerMetaData"


/*
* To update the entry
* 1- We will first delete the current/old entry
* 2- Then update the ledger Meta Data , so this way we will undo all the effects of the entry to be edited
* 3- Then add the edited entry/new entry as the old entry, and use the same uid/timestamp
* */

    fun updateLedgerMetaData_AfterEditApproved(entries: Entries){


        GlobalScope.launch(Dispatchers.IO) {


            var it= downloadLedgerMetaData()
            var totalAmout = it!!.child("total_amount").getValue<Float>()!!
            var totalEntreis = it!!.child("total_entries").getValue<Int>()!!
            var giveTakeFlag = it!!.child("give_take_flag").getValue<Boolean?>()
            var ledgerCreatedBy = it!!.child("ledgerCreatedByUID").getValue<String?>()

            calculateAmountAfterDeleting(totalAmout, giveTakeFlag,totalEntreis, oldEntry.originally_addedByUID,ledgerCreatedBy)

        }

    }

    suspend fun downloadLedgerMetaData(): DataSnapshot? {
  var data=      db_reference.child("ledgerInfo").child(ledgerUID).get().addOnCompleteListener() {
            if (it.isSuccessful) {
                if (it.result.exists()) {


                    Log.d(TAG, "downloadLedgerMetaData: Meta Data Downloaded.")
                }
            }
        }.await()
        return data
    }


   suspend private fun calculateAmountAfterDeleting(totalAmout: Float, giveTakeFlag: Boolean?, totalEntreis: Int, entrymadebyUserid: String?, ledgerCreatedBy: String?) {

        if (entrymadebyUserid.equals(ledgerCreatedBy)){
            calculateAmountAfterDeleting_createdByOwner(totalAmout,giveTakeFlag,totalEntreis,entrymadebyUserid,ledgerCreatedBy)
        }else{
            calculateAmountAfterDeleting_createdByNonOwner(totalAmout,giveTakeFlag,totalEntreis,entrymadebyUserid,ledgerCreatedBy)

        }

    }


    //GET_ENTRY_FLAG -> FALSE
    //GAVE_ENTRY_FLAG -> TRUE
 suspend   private fun calculateAmountAfterDeleting_createdByOwner(totalAmount: Float, giveTakeFlag: Boolean?, totalEntreis: Int, entrymadebyUserid: String?, ledgerCreatedBy: String?) {

        //1 -- if entry to be deleted have flag = GAVE_ENTRY_FLAG/true , it means owner gave/lend (You'll Get) money
        //now we need to delete this so we will reverse this action
        //so if ledger flag = true we will minus it from total and if amount gets negative we set ledger flag to GET_ENTRY_FLAG/false

     if(oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
            var newAmount= totalAmount-oldEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                newFlag= GET_ENTRY_FLAG
                newAmount= newAmount * (-1)
            }
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }


        //2        // if entry flag=true and ledger flag= false we will add it in total
        if (oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
            var newAmount= totalAmount+oldEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }


        //3  -- if entry to be delete have flag =false, it means owner got/owe (You'll Give) money
        //so if ledger flag = true we will add it in total

        if (oldEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
            var newAmount= totalAmount+oldEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

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
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }

    }

suspend private fun     updateLedgerMetaDataOnFireBase(newAmount: Float, newEntries: Int, newFlag: Boolean) {



    var map = HashMap<String, Any>()
    map.put("total_amount", newAmount)
    map.put("total_entries", newEntries)
    if (newFlag != null) {
        map.put("give_take_flag", newFlag)
    }


    db_reference.child("ledgerInfo").child(ledgerUID).updateChildren(map).await();


    }

  suspend  private fun calculateAmountAfterDeleting_createdByNonOwner(totalAmount: Float, giveTakeFlag: Boolean?, totalEntreis: Int, entrymadebyUserid: String?, ledgerCreatedBy: String?) {

        //1-- if entry is not made by ledger owner and entry flag is true, means he gave/lent (You'll get) money
        //but now we want to delete it so reverse this entry effect
        //so if entryFlag = true && LedgerFlag= true  ==add in total
        //it means we will add in total amount..because ledger is true means owner will get some money..and now we are deleting the
        //the entry which says nonowner gave some money, it means now owner will get this money too

      if (oldEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){

            var newAmount= totalAmount+oldEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)
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
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

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
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }



        // 4 -- if entryFlag = false && LedgerFlag = false  == add in total
        // it means nonowner got some money but now we says delete this entry so owner has to give this money too


        if (oldEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){

            var newAmount= totalAmount+oldEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)
        }

    }


}