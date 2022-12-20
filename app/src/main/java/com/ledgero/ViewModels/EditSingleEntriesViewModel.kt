package com.ledgero.ViewModels

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.DataClasses.VoiceNote
import com.ledgero.Repositories.EditSingleEntriesRepo
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.model.UtillFunctions
import com.ledgero.other.Constants.EDIT_REQUEST_REQUEST_MODE
import com.ledgero.pushnotifications.PushNotification
import com.ledgero.utils.GroupEntriesHelper
import com.ledgero.utils.GroupVoiceUploadedInterface
import io.grpc.okhttp.internal.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI

class EditSingleEntriesViewModel(private val editSingleEntriesRepo: EditSingleEntriesRepo,
            private val currentEntry:Entries):ViewModel() {

    private var TAG = "editSingleEntriesVM"

    var isAudioUpdated = false
    var isAudioDeleted = false
    var hasAudio = false
    var audioPath: String = ""



    lateinit var progressDialog: AlertDialog
    lateinit var context: Context
    fun addEntryInUnApproved_ForEdit(desc: String, amount: Float) {


        showProgressDialog()
        if (isAudioUpdated){
            if (isAudioDeleted){
                editEntrySentForApproval_audioDeleted(desc,amount)
            }
            if (!isAudioDeleted){
                var file = File(audioPath)
                if (file.exists()) {
                    editEntrySentForApproval_audioUpdated(desc, amount,file)
                }else{hideProgressDialog()
                    Toast.makeText(context, "Audio File Error Occured. Could Not Send Approval Request", Toast.LENGTH_SHORT).show()
                }
            }

        }else{
            editEntrySentForApproval_audioNotChanged(desc,amount)
        }


    }
    fun updateGroupEntry(desc: String, amount: Float, currentEntry: Entries, groupInfo: GroupInfo, audioDuration:String? ) {


        showProgressDialog()
        if (isAudioUpdated){
            if (isAudioDeleted){
                currentEntry.hasVoiceNote=false
                currentEntry.voiceNote=null

                currentEntry.amount=amount
                currentEntry.entry_desc=desc
                saveGroupEntry(groupInfo,currentEntry)
            }
            if (!isAudioDeleted){
                var file = File(audioPath)
                if (file.exists()) {
                    currentEntry.hasVoiceNote=true
                    val voiceNote= VoiceNote(file.path,Uri.fromFile(file).lastPathSegment,Integer.parseInt(audioDuration!!),null)
                    currentEntry.voiceNote= voiceNote
                    GroupEntriesHelper.uploadAuidoFile(file,groupInfo,currentEntry, object : GroupVoiceUploadedInterface{
                        override fun onVoiceUploaded(downloadURI: String, isSuccessFull: Boolean) {
                            if (isSuccessFull){

                                currentEntry.voiceNote!!.firebaseDownloadURI= downloadURI

                                currentEntry.amount=amount
                                currentEntry.entry_desc=desc
                                saveGroupEntry(groupInfo,currentEntry)

                            }else{
                                Toast.makeText(context, "Could Not Update voice note", Toast.LENGTH_SHORT).show()

                            }
                        }

                        override fun onGroupEntryAdded(isSuccessFull: Boolean) = Unit

                    })

                }else{hideProgressDialog()
                    Toast.makeText(context, "Audio File Error Occured. Could Not Send Approval Request", Toast.LENGTH_SHORT).show()
                }
            }

        }else{
            currentEntry.amount=amount
            currentEntry.entry_desc=desc
            saveGroupEntry(groupInfo,currentEntry)

        }


    }
    private fun saveGroupEntry(groupInfo: GroupInfo, entry: Entries){

        GroupEntriesHelper.updateGroupEntry(groupInfo,entry,object : GroupVoiceUploadedInterface{
            override fun onVoiceUploaded(downloadURI: String, isSuccessFull: Boolean) = Unit

            override fun onGroupEntryAdded(isSuccessFull: Boolean) {
             if (isSuccessFull){
                 hideProgressDialog()
                 Toast.makeText(context, "Entry Updated", Toast.LENGTH_SHORT).show()
             }else{
                 Toast.makeText(context, "Could not update entry. Something went wrong!", Toast.LENGTH_SHORT).show()

             }
            }

        })
    }

    fun deleteAudio() {
        isAudioDeleted = true
        isAudioUpdated = true
        hasAudio = false
        audioPath = ""
    }


    //when audio is not changed
    private fun editEntrySentForApproval_audioNotChanged(desc: String, amount: Float) {
        var newEntry: Entries = Entries()
        newEntry.apply {
            entry_desc=desc
            this.amount= amount
            requestMode = EDIT_REQUEST_REQUEST_MODE
            entryMadeBy_userID = User.userID
            entry_title = desc
            originally_addedByUID= currentEntry.originally_addedByUID
            entry_timeStamp= currentEntry.entry_timeStamp
            give_take_flag= currentEntry.give_take_flag

        }

        currentEntry.requestMode = EDIT_REQUEST_REQUEST_MODE
        viewModelScope.launch(Dispatchers.IO) {

            editSingleEntriesRepo.sendEntryForApproval_audioNotUpdated(newEntry)
            editSingleEntriesRepo.updateCurrentEntryRequestModeInApprovedEntries(currentEntry,
                EDIT_REQUEST_REQUEST_MODE)

            hideProgressDialog()
        }

    }

    //when audio is deleted
    private fun editEntrySentForApproval_audioDeleted(desc: String, amount: Float) {
        var newEntry: Entries = Entries()
        newEntry.apply {
            entry_desc=desc
            this.amount= amount
            requestMode = EDIT_REQUEST_REQUEST_MODE
            hasVoiceNote=false
            entryMadeBy_userID = User.userID
            entry_title = desc
            originally_addedByUID= currentEntry.originally_addedByUID
            entry_timeStamp= currentEntry.entry_timeStamp
            give_take_flag= currentEntry.give_take_flag

        }

        newEntry.voiceNote!!.firebaseDownloadURI = ""
        currentEntry.requestMode = EDIT_REQUEST_REQUEST_MODE
        viewModelScope.launch(Dispatchers.IO) {

            editSingleEntriesRepo.sendEntryForApproval_audioNotUpdated(newEntry)
            editSingleEntriesRepo.updateCurrentEntryRequestModeInApprovedEntries(currentEntry,
                EDIT_REQUEST_REQUEST_MODE)
hideProgressDialog()
        }
    }

    //when audio is added/updated
    private fun editEntrySentForApproval_audioUpdated(desc: String, amount: Float, newAudioFile: File) {
        var newEntry: Entries = Entries()
        newEntry.apply {
            entry_desc=desc
            this.amount= amount
            requestMode = EDIT_REQUEST_REQUEST_MODE
            hasVoiceNote=true
            entryMadeBy_userID = User.userID
            entry_title = desc
            originally_addedByUID= currentEntry.originally_addedByUID
            entry_timeStamp= currentEntry.entry_timeStamp
            give_take_flag= currentEntry.give_take_flag

        }


        currentEntry.requestMode = EDIT_REQUEST_REQUEST_MODE
        newEntry.voiceNote!!.fileName = Uri.fromFile(newAudioFile).lastPathSegment
         newEntry.voiceNote!!.localPath = newAudioFile.path

            viewModelScope.launch(Dispatchers.IO) {
            editSingleEntriesRepo.sendEntryForApproval(newEntry, newAudioFile)
                editSingleEntriesRepo.updateCurrentEntryRequestModeInApprovedEntries(currentEntry,
                    EDIT_REQUEST_REQUEST_MODE)
            hideProgressDialog()
        }
    }

    fun setProgressDialog( title: String) {
        progressDialog = UtillFunctions.setProgressDialog(context, title)

    }

    fun showProgressDialog() {
        if (progressDialog == null) {
            setProgressDialog("Sending Request For Approval")
        }
        UtillFunctions.showProgressDialog(progressDialog)
    }

    fun hideProgressDialog() {
        if (progressDialog != null) {
            UtillFunctions.hideProgressDialog(progressDialog)
        }
    }

}