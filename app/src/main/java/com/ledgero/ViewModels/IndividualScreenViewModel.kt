package com.ledgero.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.Repositories.IndividualScreenRepo
import java.io.File

class IndividualScreenViewModel(private val individualScreenRepo: IndividualScreenRepo) : ViewModel() {

        var allEntries: LiveData<ArrayList<Entries>>
        var TAG= "IndividualScreenVM"


        init {
            allEntries= getEntriesFromRepo()
        }

        fun getEntries(): LiveData<ArrayList<Entries>>{

            return allEntries

        }

    private fun deleteVoiceFromLocalDevice(entry: Entries){
        //delete voice from device
        val fdelete= File(entry.voiceNote!!.localPath)
        if (fdelete.exists()) {
            if (fdelete.delete()) {

                Log.d(TAG, "deleteVoiceFromDevice: Voice Deleted From Device")

            } else {

                Log.d(TAG, "deleteVoiceFromDevice: Voice Cannot Be Deleted From Device")
            }
        }
    }
    //this function will be called when user request delete an entry
    fun deleteEntry(pos : Int){

        //check if entry has voice note, if yes then delete it from requester user local device
        var entry= allEntries.value!!.get(pos)

        if (entry.hasVoiceNote!!){
            deleteVoiceFromLocalDevice(entry)
        }

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