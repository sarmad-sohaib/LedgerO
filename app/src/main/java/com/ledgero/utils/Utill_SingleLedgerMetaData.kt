package com.ledgero.utils

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG

class Utill_SingleLedgerMetaData(var ledgerUID:String) {

    private var db_reference = FirebaseDatabase.getInstance().reference

    private lateinit var currentEntry: Entries
    private val TAG = "Utill_LedgerMetaData"


    fun updateTotalAmount_approvedEntryDeleted(entry: Entries, isEntryEditied:Boolean= false){
        if (isEntryEditied){
            var entry= currentEntry
            var ledger:SingleLedgers= SingleLedgers()
            for (i in User.getUserSingleLedgers()!!){
                if (i.ledgerUID.equals(ledgerUID)) ledger=i
            }
            for (i in ledger!!.entries!!){
                if (i.entryUID.equals(currentEntry.entryUID)) currentEntry=i

            }
        }else{currentEntry=entry}


        db_reference.child("ledgerInfo").child(ledgerUID).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.exists()) {

                    var totalAmout = it.result.child("total_amount").getValue<Float>()!!
                    var totalEntreis = it.result.child("total_entries").getValue<Int>()!!
                    var giveTakeFlag = it.result.child("give_take_flag").getValue<Boolean?>()
                    var ledgerCreatedBy = it.result.child("ledgerCreatedByUID").getValue<String?>()

                    calculateAmountAfterDeleting(totalAmout,
                        giveTakeFlag,
                        totalEntreis,
                        entry.originally_addedByUID,ledgerCreatedBy)

                }
            }
        }

    }

    private fun calculateAmountAfterDeleting(totalAmout: Float, giveTakeFlag: Boolean?, totalEntreis: Int, entrymadebyUserid: String?, ledgerCreatedBy: String?) {

        if (entrymadebyUserid.equals(ledgerCreatedBy)){
            calculateAmountAfterDeleting_createdByOwner(totalAmout,giveTakeFlag,totalEntreis,entrymadebyUserid,ledgerCreatedBy)
        }else{
            calculateAmountAfterDeleting_createdByNonOwner(totalAmout,giveTakeFlag,totalEntreis,entrymadebyUserid,ledgerCreatedBy)

        }

    }

    private fun calculateAmountAfterDeleting_createdByNonOwner(totalAmount: Float, giveTakeFlag: Boolean?, totalEntreis: Int, entrymadebyUserid: String?, ledgerCreatedBy: String?) {

        //1-- if entry is not made by ledger owner and entry flag is GAVE_ENTRY_FLAG/true, means he gave/lent (You'll get) money
                //but now we want to delete it so reverse this entry effect
                //so if entryFlag = GAVE_ENTRY_FLAG/true && LedgerFlag= GAVE_ENTRY_FLAG/true  -->add in total
                        //it means we will add in total amount..because ledger is true means owner will get some money..and now we are deleting the
                        //the entry which says nonowner gave some money, it means now owner will get this money too

        if (currentEntry.give_take_flag==GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){

                var newAmount= totalAmount+currentEntry.amount!!
                var newFlag= giveTakeFlag
                var newEntries = totalEntreis-1
                updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)
        }


        //2 --  if entryFlag = GAVE_ENTRY_FLAG/true && LedgerFlag = /GET_ENTRY_FLAGfalse ==minus from total
                    //it means nonowner says delete the amount which I gave..it means now owner owes him less money. So after minus check if amount is
                                                        //is negative then set LedgerFlag = GAVE_ENTRY_FLAG/true

        if(currentEntry.give_take_flag==GAVE_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
            var newAmount= totalAmount-currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                newFlag= GAVE_ENTRY_FLAG
                newAmount= newAmount * (-1)
            }
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }


        // 3  -- if entryFlag = GET_ENTRY_FLAG/false && LedgerFlag = GAVE_ENTRY_FLAG/true == minus from total
                    // it means nonowner got/owe some for this entry but now we delete the entry and now owner will get less money
                            //so after minus we need to check and if amount becomes negative we set LedgerFlag = GET_ENTRY_FLAG/false

        if(currentEntry.give_take_flag==GET_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
            var newAmount= totalAmount-currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                newFlag= GET_ENTRY_FLAG
                newAmount= newAmount * (-1)
            }
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }



        // 4 -- if entryFlag = GET_ENTRY_FLAG/false && LedgerFlag = GET_ENTRY_FLAG/false  == add in total
                    // it means nonowner got some money but now we says delete this entry so owner has to give this money too


        if (currentEntry.give_take_flag==GET_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){

            var newAmount= totalAmount+currentEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)
        }

    }

    private fun calculateAmountAfterDeleting_createdByOwner(
        totalAmount: Float,
        giveTakeFlag: Boolean?,
        totalEntreis: Int,
        entrymadebyUserid: String?,
        ledgerCreatedBy: String?
    ) {

        //1 -- if entry to be deleted have flag = GAVE_ENTRY_FLAG/true , it means owner gave/lend (You'll Get) money
                //now we need to delete this so we will reverse this action
                //so if ledger flag = GAVE_ENTRY_FLAG/true we will minus it from total and if amount gets negative we set ledger flag to GET_ENTRY_FLAG/false

        if(currentEntry.give_take_flag==GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
            var newAmount= totalAmount-currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                newFlag= GET_ENTRY_FLAG
                newAmount= newAmount * (-1)
            }
            var newEntries = totalEntreis-1
        updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }


        //2        // if entry flag= GAVE_ENTRY_FLAG/true and ledger flag= GET_ENTRY_FLAG/false we will add it in total
        if (currentEntry.give_take_flag==GAVE_ENTRY_FLAG && giveTakeFlag==GET_ENTRY_FLAG){
            var newAmount= totalAmount+currentEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }


        //3  -- if entry to be delete have flag =GET_ENTRY_FLAG/false, it means owner got/owe (You'll Give) money
                //so if ledger flag = GAVE_ENTRY_FLAG/true we will add it in total

        if (currentEntry.give_take_flag==GET_ENTRY_FLAG && giveTakeFlag==GAVE_ENTRY_FLAG){
            var newAmount= totalAmount+currentEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }

        //4 -- if entry flag = GET_ENTRY_FLAG/false and ledger flag GET_ENTRY_FLAG/false then we will minus it from total and if amounts get negative we set ledger to GAVE_ENTRY_FLAG/true
        if(currentEntry.give_take_flag==GET_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
            var newAmount= totalAmount-currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                newFlag= GAVE_ENTRY_FLAG
                newAmount= newAmount * (-1)
            }
            var newEntries = totalEntreis-1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)

        }

    }


    fun updateTotalAmount_newEntryAdded(entry: Entries) {

        currentEntry = entry
        db_reference.child("ledgerInfo").child(ledgerUID).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.exists()) {

                    var totalAmout = it.result.child("total_amount").getValue<Float>()!!
                    var totalEntreis = it.result.child("total_entries").getValue<Int>()!!
                    var giveTakeFlag = it.result.child("give_take_flag").getValue<Boolean?>()
                    var ledgerCreatedBy = it.result.child("ledgerCreatedByUID").getValue<String?>()

                    calculateAmount(totalAmout,
                        giveTakeFlag,
                        totalEntreis,
                        entry.originally_addedByUID,ledgerCreatedBy)

                }
            }
        }

    }

    private fun calculateAmount(
        totalAmout: Float,
        giveTakeFlag: Boolean?,
        totalEntreis: Int,
        entrymadebyUserid: String?,
        ledgerCreatedBy: String?
    ) {

        //check if entry is made by the user who created ledger or not


        //this entry is made by the user who created ledger
        if (entrymadebyUserid!!.equals(ledgerCreatedBy)) {
            calculateAmount_entryCreatedByOwner(totalAmout, giveTakeFlag, totalEntreis)
        } else {
            calculateAmount_entryCreatedByNonOwner(totalAmout, giveTakeFlag, totalEntreis)

        }


    }


    private fun calculateAmount_entryCreatedByOwner(
        totalAmout: Float,
        giveTakeFlag: Boolean?,
        totalEntreis: Int
    ) {
        // entry is being made by the owner of ledger


        //1 --  if -entry flag is GET_ENTRY_FLAG/false it means he got the money so if $ledger flag is GET_ENTRY_FLAG/false we will add this amount
        //in total because it means he is already in debt or he owe(You'll Give)

        if (currentEntry.give_take_flag == GET_ENTRY_FLAG && giveTakeFlag == GET_ENTRY_FLAG) {
            var newAmount = totalAmout + currentEntry.amount!!
            var newTotalEntries = totalEntreis + 1

            updateLedgerMetaDataOnFireBase(newAmount, newTotalEntries, giveTakeFlag)
        }


        //2 --  if  -entry flag is GET_ENTRY_FLAG/false it means he got money so if $ledger flag is GAVE_ENTRY_FLAG/true we will minus this amount from
        //total because he had lended/Gave(You'll Get) money and now he received some..so we will minus it from total
        //money he gave -- And after minus if amount become negative then we will set the $ledger flag to GET_ENTRY_FLAG/false and if
        //amount becomes 0 we will set the $ledger flag to null means all clear 00 ,00
        if (currentEntry.give_take_flag == GET_ENTRY_FLAG && giveTakeFlag == GAVE_ENTRY_FLAG) {
            var newAmount = totalAmout - currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){newFlag=GET_ENTRY_FLAG
                            newAmount= newAmount * (-1)
            }
            if (newAmount==0f){newFlag=null}

            var newTotalEntries = totalEntreis + 1

            updateLedgerMetaDataOnFireBase(newAmount, newTotalEntries, newFlag)
        }

        //3 -- if -entry flag is GAVE_ENTRY_FLAG/true it means he gave the money so if $ledger flag is GAVE_ENTRY_FLAG/true we will add this amount in total
        //because he already have lended/Gave(You'll Get) money and how gave some more. So, we will add in total
        if (currentEntry.give_take_flag == GAVE_ENTRY_FLAG && giveTakeFlag == GAVE_ENTRY_FLAG) {
            var newAmount = totalAmout + currentEntry.amount!!
            var newFlag= giveTakeFlag
            var newTotalEntries = totalEntreis + 1
            updateLedgerMetaDataOnFireBase(newAmount, newTotalEntries, newFlag)
        }



        //4 -- if -entry flag is GAVE_ENTRY_FLAG/true it means he gave the money so if $ledger flag is GET_ENTRY_FLAG/false then we will minus it from total
        //because he lended/gave some money but he owed/get (You'll Give) money..So, now he have paid money we will minus
        //-- And after minus if amount become negative then we will set the $ledger flag to GAVE_ENTRY_FLAG/true because now he has surplus his debt
        // and he will get some money now and if amount becomes 0 we will set the $ledger flag to null means all clear 00 ,00

        if (currentEntry.give_take_flag == GAVE_ENTRY_FLAG && giveTakeFlag == GET_ENTRY_FLAG) {
            var newAmount = totalAmout - currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){newFlag=GAVE_ENTRY_FLAG
                                newAmount=newAmount* (-1)
            }
            if (newAmount==0f){newFlag=null}

            var newTotalEntries = totalEntreis + 1

            updateLedgerMetaDataOnFireBase(newAmount, newTotalEntries, newFlag)
        }
        Log.d(TAG, "calculateAmount_entryCreatedByOwner: ended")
    }


    private fun calculateAmount_entryCreatedByNonOwner(
        totalAmout: Float,
        giveTakeFlag: Boolean?,
        totalEntreis: Int
    ) {


        //1 -- if -entry flag is GET_ENTRY_FLAG/false it means nonowner got(You'll Gave) the money and if $ledger flag is GET_ENTRY_FLAG/false it means owner owes(You'll Give)
                // so we will minus the amount from the total because owner owes the money and nonowner says he got the money..so we
                //will minus it from total -- After minus if amount becomes negative then we will set the $ledger flag to GAVE_ENTRY_FLAG/true and if amount
                //amount becomes 0 then flag to null
        if(currentEntry.give_take_flag== GET_ENTRY_FLAG && giveTakeFlag==GET_ENTRY_FLAG){
            var newAmount = totalAmout- currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                            newFlag=true
                newAmount= newAmount * (-1)

            }
            if (newAmount==0f){
                newFlag=null

            }
            var newEntries= totalEntreis+1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)


        }

        //2 -- if -entry flag is GET_ENTRY_FLAG/false it means nonowner got(You'll Gave) the money and if $ledger flag is GAVE_ENTRY_FLAG/true then it means we will add the
                //amount in total because

        if(currentEntry.give_take_flag==GET_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
            var newAmount = totalAmout+ currentEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries= totalEntreis+1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)
}

        //3 -- if -entry flag is GAVE_ENTRY_FLAG/true it means nonowner gave(You'll Get) the money and if $ledger flag is GET_ENTRY_FLAG/false it means owner owes(You'll Give) the
                //money and nononwer says he has gave more money...so we will add it in total
        if(currentEntry.give_take_flag== GAVE_ENTRY_FLAG && giveTakeFlag== GET_ENTRY_FLAG){
            var newAmount = totalAmout+ currentEntry.amount!!
            var newFlag= giveTakeFlag
            var newEntries= totalEntreis+1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)
        }



        //4 -- if -entry flag is GAVE_ENTRY_FLAG/true it means nonowner gave(You'll Get) the money and if $ledger flag is GAVE_ENTRY_FLAG/true lend/gave(You'll Get) the money
                //so now nononwer says he gave some money, so we will minus it from total-- After minus if amount becomes negative $ledger flag
                //will be set to GET_ENTRY_FLAG/false because now owner owes some money and if amount becomes 0 $ledger flag set to null

        if(currentEntry.give_take_flag==GAVE_ENTRY_FLAG && giveTakeFlag== GAVE_ENTRY_FLAG){
            var newAmount = totalAmout- currentEntry.amount!!
            var newFlag= giveTakeFlag
            if (newAmount<0){
                newFlag=GET_ENTRY_FLAG
                newAmount= newAmount * (-1)

            }
            if (newAmount==0f){
                newFlag=null

            }
            var newEntries= totalEntreis+1
            updateLedgerMetaDataOnFireBase(newAmount,newEntries,newFlag)


        }



    }


    private fun updateLedgerMetaDataOnFireBase(
        newAmount: Float,
        newTotalEntries: Int,
        giveTakeFlag: Boolean?
    ) {

        var map = HashMap<String, Any>()
        map.put("total_amount", newAmount)
        map.put("total_entries", newTotalEntries)
        if (giveTakeFlag != null) {
            map.put("give_take_flag", giveTakeFlag)
        }


        db_reference.child("ledgerInfo").child(ledgerUID).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful){
                Log.d(TAG, "updateLedgerMetaDataOnFireBase: Ledger Meta Data Updated")
            }
        }
    }
}