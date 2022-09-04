package com.ledgero.Repositories

import androidx.lifecycle.LiveData
import com.ledgero.DAOs.IndividualScreenDAO
import com.ledgero.DAOs.UnApproveEntriesDAO
import com.ledgero.DataClasses.Entries

class UnApprovedEntriesRepo(private val unApproveEntriesDAO: UnApproveEntriesDAO)  {

    fun getEntries(): LiveData<ArrayList<Entries>> {

        return unApproveEntriesDAO.getUnApprovedEntries()

    }

    fun removeListener(){
       unApproveEntriesDAO.removeListener()
    }
    fun deleteEntry(pos: Int){
        unApproveEntriesDAO.deleteEntry(pos)
    }

}