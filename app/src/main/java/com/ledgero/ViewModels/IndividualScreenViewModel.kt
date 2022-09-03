package com.ledgero.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.Repositories.IndividualScreenRepo

class IndividualScreenViewModel(private val individualScreenRepo: IndividualScreenRepo) : ViewModel() {

        var allEntries: LiveData<ArrayList<Entries>>


        init {
            allEntries= getEntriesFromRepo()
        }

        fun getEntries(): LiveData<ArrayList<Entries>>{

            return allEntries

        }

    fun deleteEntry(pos : Int){
        individualScreenRepo.deleteEntry(pos)
    }
   private fun getEntriesFromRepo():LiveData<ArrayList<Entries>>{

        return individualScreenRepo.getEntries()
    }

    fun addNewEntry(entry: Entries){

        individualScreenRepo.addNewEntry(entry)
    }
    fun removeListener(){
        individualScreenRepo.removeListener()
    }

}