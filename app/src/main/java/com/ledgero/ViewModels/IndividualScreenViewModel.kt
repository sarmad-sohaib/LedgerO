package com.ledgero.ViewModels

import android.content.ContextWrapper
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
import com.ledgero.Repositories.IndividualScreenRepo
import java.io.File

class IndividualScreenViewModel(private val individualScreenRepo: IndividualScreenRepo) : ViewModel() {

        var allEntries: LiveData<ArrayList<Entries>>
        var TAG= "IndividualScreenVM"
        var totalAmount:LiveData<Float>
        var giveTakeFlag:LiveData<Boolean?>


        init {
            individualScreenRepo.getMetaData()
            totalAmount= individualScreenRepo.getTotalAmount()
            giveTakeFlag= individualScreenRepo.getGiveTakeFlag()

            allEntries= getEntriesFromRepo()
        }

        fun getEntries(): LiveData<ArrayList<Entries>>{

            return allEntries

        }

    fun getLedgerTotalAmount():LiveData<Float>{
        return totalAmount
    }
    fun getLedgerGiveTakeFlag():LiveData<Boolean?>{
        return giveTakeFlag
    }


    private fun deleteVoiceFromLocalDevice(entry: Entries){
        //delete voice from device
        var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)


        val fdelete= File( contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+entry.voiceNote!!.fileName
        )
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
    fun removeLedgerMetaDataListener(){
        individualScreenRepo.removeLedgerMetaDataListener()
    }

}