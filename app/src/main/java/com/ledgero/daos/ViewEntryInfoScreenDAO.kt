package com.ledgero.daos

import android.content.ContextWrapper
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ViewEntryInfoScreenDAO(private val ledgerUID: String) {
    private val TAG = "UnApproveEntriesDAO"
    private  var db_reference = FirebaseDatabase.getInstance().reference

    private var dataDownload: MutableLiveData<Int>?=MutableLiveData<Int>()
    private var totalFileSize: Long = 0L
    private var bytesTransferred: Long =0L

    fun getVoiceFile(entry: Entries): MutableLiveData<Int>? {
        downloadVoiceFromFirestoreStorage(entry)
        return dataDownload
    }

  private  fun downloadVoiceFromFirestoreStorage(entry:Entries){
      var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)
        var file = File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+entry.voiceNote!!.fileName)

      checkAndDeleteOlderVersionOfVoice(entry.voiceNote!!.fileName!!,entry)
        //Creating a reference to the link
    var httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(entry.voiceNote!!.firebaseDownloadURI!!)

        //Getting the file from the server
        httpsReference.getFile(file).addOnProgressListener { taskSnapshot ->

            totalFileSize= taskSnapshot.totalByteCount
           bytesTransferred= taskSnapshot.bytesTransferred
            dataDownload!!.value= ((bytesTransferred/totalFileSize)*100).toInt()

        }.addOnCompleteListener {
            if (it.isSuccessful){
                dataDownload!!.value=100
            }
        }

    }

    private fun checkAndDeleteOlderVersionOfVoice(fileName:String, entry: Entries){

        GlobalScope.launch(Dispatchers.IO) {
            var contextWrapper= ContextWrapper(MainActivity.getMainActivityInstance().applicationContext)

            //check last digit of file to check version of audio
            var version= fileName.dropLast(4).last().toString().toInt()
            var file:File?
            var fileVersionName= fileName.dropLast(5).toString()
            for (i in 1..version){
          //if entry has voice note and only latest version is in local device then break the loop
            if (i==version && entry.hasVoiceNote!!){
                break
            }

                var name= fileVersionName+i+".mp3"

                file = File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+name)

            if (file.exists()){
                file.delete()
            }
            }
        }

    }


    }
