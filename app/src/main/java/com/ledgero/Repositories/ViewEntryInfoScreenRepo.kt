package com.ledgero.Repositories

import androidx.lifecycle.LiveData
import com.ledgero.DAOs.ViewEntryInfoScreenDAO
import com.ledgero.DataClasses.Entries


class ViewEntryInfoScreenRepo(private val viewEntryInfoScreenDAO: ViewEntryInfoScreenDAO) {


    fun getVoiceFile(entry:Entries): LiveData<Int>? {

       return viewEntryInfoScreenDAO.getVoiceFile(entry)
    }
}