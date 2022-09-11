package com.ledgero.DAOs

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
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
        var file = File(entry.voiceNote!!.localPath)

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