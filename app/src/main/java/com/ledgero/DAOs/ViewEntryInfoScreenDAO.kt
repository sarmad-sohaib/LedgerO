package com.ledgero.DAOs

import android.content.ContextWrapper
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
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
        var file = File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+entry.voiceNote!!.fileName)

        //Creating a reference to the link
    var httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(entry.voiceNote!!.firebaseDownloadURI!!)

        //Getting the file from the server
        httpsReference.getFile(file).addOnProgressListener { taskSnapshot ->

            totalFileSize= taskSnapshot.totalByteCount
           bytesTransferred= taskSnapshot.bytesTransferred
            dataDownload!!.value= ((bytesTransferred/totalFileSize)*100).toInt()

        }.addOnCompleteListener(){
            if (it.isSuccessful){
                dataDownload!!.value=100
            }
        }

    }
}