package com.ledgero.Repositories

import androidx.lifecycle.LiveData
import com.ledgero.DAOs.CanceledEntriesDAO
import com.ledgero.DataClasses.Entries

class CanceledEntriesRepo(private val canceledEntriesDAO: CanceledEntriesDAO)  {

    fun getEntries(): LiveData<ArrayList<Entries>> {

        return canceledEntriesDAO.getCanceledEntries()

    }

    fun removeListener(){
        canceledEntriesDAO.removeListener()
    }
    fun deleteEntry(pos: Int){
        canceledEntriesDAO.deleteCanceledEntry(pos)
    }
    fun requestAgain(pos:Int){
        canceledEntriesDAO.requestAgain(pos)
    }


}