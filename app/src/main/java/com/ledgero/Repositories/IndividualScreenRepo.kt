package com.ledgero.Repositories

import androidx.lifecycle.LiveData
import com.ledgero.DAOs.IndividualScreenDAO
import com.ledgero.DataClasses.Entries

class IndividualScreenRepo(private val individualScreenDAO: IndividualScreenDAO) {

    fun getEntries():LiveData<ArrayList<Entries>>{

        return individualScreenDAO.getEntries()

    }

    fun addNewEntry(entry: Entries){
        individualScreenDAO.addNewEntry(entry)
    }
    fun removeListener(){
        individualScreenDAO.removeListener()
    }
    fun deleteEntry(pos: Int){
        individualScreenDAO.deleteEntry(pos)
    }
}