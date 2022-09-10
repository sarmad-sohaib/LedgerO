package com.ledgero.Repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.ledgero.DAOs.CanceledEntriesDAO
import com.ledgero.DataClasses.Entries
import java.io.File

class CanceledEntriesRepo(private val canceledEntriesDAO: CanceledEntriesDAO)  {
    private val TAG:String= "CanceledEntriesRep"

    fun getEntries(): LiveData<ArrayList<Entries>> {

        return canceledEntriesDAO.getCanceledEntries()

    }

    private fun deleteVoiceFromDevice(entry:Entries)
    {
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
    fun removeListener(){
        canceledEntriesDAO.removeListener()
    }

    fun deleteEntryWithVoice(entry:Entries){

        //check if entry to be delete has voice note or not
        //if it has then delete it from local device
        deleteVoiceFromDevice(entry)
        canceledEntriesDAO.deleteCanceledEntryWithVoice(entry)

    }
    fun deleteEntry(pos: Int){
        canceledEntriesDAO.deleteCanceledEntry(pos)
    }
    fun requestAgain(pos:Int){
        canceledEntriesDAO.requestAgain(pos)
    }


}