package com.ledgero.Repositories

import androidx.lifecycle.LiveData
import com.ledgero.DAOs.IndividualScreenDAO
import com.ledgero.DataClasses.Entries

class IndividualScreenRepo(private val individualScreenDAO: IndividualScreenDAO) {

    fun getEntries():LiveData<ArrayList<Entries>>{

        return individualScreenDAO.getEntries()

    }

    fun startListeningForUnApprovedEntries(): LiveData<Long>{

        return individualScreenDAO.startListeningForUnApprovedEntries()
    }
    fun stopListeningForUnApprovedEntries(){
        individualScreenDAO.stopListeningForUnApprovedEntries()

    }


    fun addNewEntry(entry: Entries){

        if (entry.hasVoiceNote!!){
            individualScreenDAO.uploadVoiceNoteThenAddNewEntry(entry)
        }else{

        individualScreenDAO.addNewEntry(entry)
        }
        }
    fun removeListener(){
        individualScreenDAO.removeListener()
    }
    fun deleteEntry(pos: Int){
        individualScreenDAO.deleteEntry(pos)
    }

    fun getMetaData(){
        individualScreenDAO.getLedgerMetaData()
    }

    fun getTotalAmount():LiveData<Float>{
       return individualScreenDAO.getTotalAmount()
    }

    fun getGiveTakeFlag():LiveData<Boolean?>{
        return individualScreenDAO.getGiveTakeFlag()
    }

    fun getLedgerCreatedBy():String{
        return individualScreenDAO.getLedgerCreatedBy()
    }


    fun removeLedgerMetaDataListener(){
        individualScreenDAO.removeLedgerMetaDataListener()
    }

    fun startListeningForCancelledEntries(): LiveData<Long> {


        return individualScreenDAO.startListeningForCancelledEntries()

    }

    fun stopListeningForCancelledEntries() {

        individualScreenDAO.stopListeningForCancelledEntries()
    }


}