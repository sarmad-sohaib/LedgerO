package com.ledgero.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.DataClasses.VoiceNote
import com.ledgero.Interfaces.EntryDetailInterface
import com.ledgero.R
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.groupLedger.singleGroup.GroupEntry
import com.ledgero.model.UtillFunctions
import com.ledgero.other.Constants.CASH_IN
import com.ledgero.other.Constants.CASH_OUT
import com.ledgero.other.Constants.ENTRY_ADDED_GROUP
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG
import com.ledgero.utils.GroupEntriesHelper
import com.ledgero.utils.GroupVoiceUploadedInterface
import com.ledgero.utils.Utill_AddNewEntryDetail

import kotlinx.android.synthetic.main.fragment_add_new_entry_detail.view.*
import kotlinx.coroutines.tasks.await
import java.io.File


class AddNewEntryDetail(
    var ledger: SingleLedgers?,
    var isGroupEntry: Boolean,
    var groupInfo: GroupInfo?
) : Fragment(), EntryDetailInterface {
    override lateinit var calculatorLayout: View
    override lateinit var amountTextTV: EditText
    override lateinit var totalAmount: EditText

    override lateinit var myContext: Context


    lateinit var utill: Utill_AddNewEntryDetail
    override var mLedger: SingleLedgers? = ledger
    override var mGroupInfo: GroupInfo? = groupInfo
    override lateinit var audioLayout: ConstraintLayout
    override lateinit var audioPlayBtn: Button
    override lateinit var recordBtn: Button
    override lateinit var recordDuartionText: TextView
    override lateinit var recordMaxLimitText: TextView
    override lateinit var hintRecordText: TextView
    override lateinit var recordSeekBar: SeekBar

    private val dbReference = FirebaseDatabase.getInstance().reference

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        myContext = requireContext()

        var view = inflater.inflate(R.layout.fragment_add_new_entry_detail, container, false)
        var entryMode: Boolean? = null // mode tell if user pressed Got or Gave

        if (ledger!= null){
            utill = Utill_AddNewEntryDetail(this)

        }else{
            utill = Utill_AddNewEntryDetail(this)

        }
        amountTextTV = view.tv_amount_add_new_entry
        totalAmount = view.tv_Totalamount_view_entry

        setFragmentResultListener("addEntryBtn") { addEntryBtn, bundle ->

            entryMode = bundle.getBoolean("mode")

            if (entryMode == GET_ENTRY_FLAG)//means user pressed GOT  represent false
            {
                view.view_entry_entry_title.setTextColor(Color.GREEN)

            }
            if (entryMode == GAVE_ENTRY_FLAG) { //represent true

                view.view_entry_entry_title.setTextColor(Color.RED)

            }

        }


        setCalculatorBtnListeners(view)


        view.bt_add_new_entry.setOnClickListener {


            if (utill.audioRecordUtill.isAudioRecording) {
                Toast.makeText(
                    context,
                    "You Are Recording A Voice...Stop Recording to Save",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                if (!totalAmount.text.isNullOrBlank() && !totalAmount.text.toString()
                        .equals("Err")
                ) {

                    if (!view.tv_description_view_entry.text.isNullOrBlank()) {

//                        Toast.makeText(context, "Adding New Entry To Ledger", Toast.LENGTH_SHORT)
//                            .show()

                        val amount: Float = java.lang.Float.valueOf(totalAmount.text.toString())
                        if (amount <= 0f) {
                            Toast.makeText(context, "Entry Amount cannot be 0", Toast.LENGTH_SHORT)
                                .show()
                            return@setOnClickListener
                        }
                        //group entry
                        if (isGroupEntry) {

                            val progress = UtillFunctions.setProgressDialog(
                                requireContext(),
                                "Adding New Entry In Group"
                            )
                            UtillFunctions.showProgressDialog(progress)
                            var des = view.tv_description_view_entry.text.toString()

                            var title = if (des.length > 16) des.subSequence(0, 15)
                                .toString() else des.toString()


                            var flag =
                                if (entryMode == CASH_IN) CASH_IN else CASH_OUT


                            var entry = GroupEntry(flag).apply {
                                this.amount = amount
                                this.entry_desc = des
                                this.entry_title = title
                                this.entry_timeStamp = 1111
                                this.entryMadeBy_userID = User.userID
                            }


                            if (utill.audioRecordUtill.hasVoiceNote) {
                                var voiceNote =
                                    VoiceNote(
                                        utill.audioRecordUtill.localPath!!,
                                        utill.audioRecordUtill.VoicefileName,
                                        utill.audioRecordUtill.getAudioDuration().toInt(),
                                        null
                                    )
                                entry.hasVoiceNote = true
                                entry.voiceNote = voiceNote

                                dbReference.child("tempNode")
                                    .setValue(mapOf("timeStamp" to ServerValue.TIMESTAMP))
                                    .addOnCompleteListener {
                                        dbReference.child("tempNode").get().addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                val timeStamp =
                                                    it.result.child("timeStamp").value.toString()
                                                        .toLong()
                                                entry.entry_timeStamp = timeStamp
                                                val entryKey = dbReference.child("groupEntries")
                                                    .child(groupInfo!!.groupUID).push().key
                                                entry.entryUID = entryKey

                                                GroupEntriesHelper.uploadAuidoFile(File(entry.voiceNote!!.localPath!!),groupInfo!!,entry, object : GroupVoiceUploadedInterface{
                                                    override fun onVoiceUploaded(
                                                        downloadURI: String,
                                                        isSuccessFull: Boolean
                                                    ) {
                                                        if (isSuccessFull){
                                                            entry.voiceNote!!.firebaseDownloadURI=downloadURI
                                                            dbReference.child("groupEntries")
                                                                .child(groupInfo!!.groupUID)
                                                                .child(entry.entryUID.toString()).setValue(entry)
                                                                .addOnCompleteListener {
                                                                    UtillFunctions.hideProgressDialog(progress)
                                                                    if (it.isSuccessful) {

                                                                        Log.d(
                                                                            "AddNewEntry",
                                                                            " line 175: New Group Entry Added "
                                                                        )
                                                                        parentFragmentManager.popBackStack()
                                                                    } else {
                                                                        Toast.makeText(
                                                                            requireContext(),
                                                                            "Sorry! Something went wrong! Please try later!",
                                                                            Toast.LENGTH_LONG
                                                                        )
                                                                            .show()
                                                                        Log.d(
                                                                            "AddNewEntry",
                                                                            " line 175: Cannot Add Group Entry "
                                                                        )

                                                                    }
                                                                }
                                                        }else{
                                                            UtillFunctions.hideProgressDialog(progress)

                                                        }
                                                    }

                                                    override fun onGroupEntryAdded(isSuccessFull: Boolean)= Unit

                                                })

                                            }
                                        }
                                    }



                            }else{
                                dbReference.child("tempNode")
                                    .setValue(mapOf("timeStamp" to ServerValue.TIMESTAMP))
                                    .addOnCompleteListener {
                                        dbReference.child("tempNode").get().addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                val timeStamp =
                                                    it.result.child("timeStamp").value.toString()
                                                        .toLong()
                                                entry.entry_timeStamp = timeStamp
                                                val entryKey = dbReference.child("groupEntries")
                                                    .child(groupInfo!!.groupUID).push().key
                                                entry.entryUID = entryKey
                                                dbReference.child("groupEntries")
                                                    .child(groupInfo!!.groupUID)
                                                    .child(entry.entryUID.toString()).setValue(entry)
                                                    .addOnCompleteListener {
                                                        UtillFunctions.hideProgressDialog(progress)
                                                        if (it.isSuccessful) {
                                                            GroupEntriesHelper.sendNotificationToAllMember(groupInfo!!,
                                                                ENTRY_ADDED_GROUP)
                                                            Log.d(
                                                                "AddNewEntry",
                                                                " line 175: New Group Entry Added "
                                                            )
                                                            parentFragmentManager.popBackStack()
                                                        } else {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Sorry! Something went wrong! Please try later!",
                                                                Toast.LENGTH_LONG
                                                            )
                                                                .show()
                                                            Log.d(
                                                                "AddNewEntry",
                                                                " line 175: Cannot Add Group Entry "
                                                            )

                                                        }
                                                    }

                                            }
                                        }
                                    }
                            }






                        } else {

                            var des = view.tv_description_view_entry.text.toString()

                            var title = if (des.length > 16) des.subSequence(0, 15)
                                .toString() else des.toString()


                            var flag =
                                if (entryMode == GAVE_ENTRY_FLAG) GAVE_ENTRY_FLAG else GET_ENTRY_FLAG


                            var entry = Entries(
                                amount, flag, des,
                                title, 111111, false,
                                User.userID, "", 1,
                                false, null, User.userID
                            )

                            if (utill.audioRecordUtill.hasVoiceNote) {
                                var voiceNote =
                                    VoiceNote(
                                        utill.audioRecordUtill.localPath!!,
                                        utill.audioRecordUtill.VoicefileName,
                                        utill.audioRecordUtill.getAudioDuration().toInt(),
                                        null
                                    )
                                entry.hasVoiceNote = true
                                entry.voiceNote = voiceNote
                            }

                            IndividualLedgerScreen.instanceObject!!.viewModel.addNewEntry(entry)
                            var frag =
                                UnApprovedEntriesScreen(IndividualLedgerScreen.instanceObject!!.currentSelectedLedgerUID.toString())


                            parentFragmentManager.popBackStack()
                            parentFragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.fl_fragment_container_main, frag)
                                .commit()

                        }
                    } else {
                        Toast.makeText(context, "No Description Added!!", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    Toast.makeText(context, "Please Enter Amount!!", Toast.LENGTH_SHORT).show()

                }
            }
        }




        amountTextTV.inputType = InputType.TYPE_NULL // disable soft input


        amountTextTV.tv_amount_add_new_entry.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                //this will close the soft keyboard if its open
                // hide virtual keyboard
                hideKeyboard()
                if (view.calculator_layout_add_new_entry_include.visibility == View.GONE) {
                    view.calculator_layout_add_new_entry_include.visibility = View.VISIBLE

                }

                return false
            }
        })





        view.tv_description_view_entry.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                view.calculator_layout_add_new_entry_include.visibility = View.GONE
                return false

            }

        })


        amountTextTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (view.tv_amount_add_new_entry.text.isNullOrEmpty()) {
                    view.entryInfoScrollView_add_new_entry_screen.visibility = View.GONE
                } else {
                    view.entryInfoScrollView_add_new_entry_screen.visibility = View.VISIBLE

                }
            }

        })


        //record audio ui operations
        audioLayout = view.audioPlay_layout_addNewEntry
        audioPlayBtn = view.btn_play_recordVoice_addNewEntryDetail
        recordBtn = view.btn_recordVoice_addNewEntryDetail
        recordDuartionText = view.audioRecordDuration_tv_addNewEntryDetail
        recordMaxLimitText = view.audioMaxLimit_tv_addNewEntryDetail
        hintRecordText = view.hint_recordAudio_addNewEntry
        recordSeekBar = view.seekBar_addNewEntry


        recordBtn.setOnClickListener {
            @RequiresApi(Build.VERSION_CODES.S)
            if (utill.audioRecordUtill.isAudioRecording) {

                //stop audio Recording
                utill.audioRecordUtill.stopAudioRecording()
                utill.audioRecordUtill.stopTimer()
                utill.audioRecordUtill.isAudioRecording = false
                recordBtn.foreground = getDrawable(requireContext(), R.drawable.ic_microphone)
                audioLayout.visibility = View.VISIBLE
                hintRecordText.visibility = View.GONE
                recordMaxLimitText.text = "Max 60s"

                // set audioDuration
                view.audioRecordDuration_tv_addNewEntryDetail.text =
                    utill.audioRecordUtill.getAudioDuration() + "s"
            } else {
                //start audio Recording
                if (utill.audioRecordUtill.isMicPresent()) {
                    if (!utill.audioRecordUtill.isMicPermission()) {
                        Toast.makeText(
                            context,
                            "Please Allow Us To Use Your Device Mice",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        utill.audioRecordUtill.startAudioRecording()
                        utill.audioRecordUtill.startTimer(recordMaxLimitText)
                        utill.audioRecordUtill.isAudioRecording = true
                        audioLayout.visibility = View.GONE
                        hintRecordText.text =
                            "Your voice note is recording. Tap recording button to stop"
                        hintRecordText.visibility = View.VISIBLE

                        recordBtn.foreground =
                            getDrawable(requireContext(), R.drawable.ic_sound_waveform)
                    }
                } else {
                    Toast.makeText(context, "Sorry! No Working Mic Found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }


        audioLayout.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {

                var alertDialog = utill.audioRecordUtill.getAlertDialogForDeletingVoice()
                alertDialog.show()

                return true
            }

        })


        audioPlayBtn.setOnClickListener {

            if (utill.audioRecordUtill.isAudioPlaying) {
                utill.audioRecordUtill.stopPlayingAudio()
                audioPlayBtn.foreground = getDrawable(requireContext(), R.drawable.ic_play_button)
                utill.audioRecordUtill.isAudioPlaying = false
            } else {
                utill.audioRecordUtill.startPlayingAudio(
                    audioPlayBtn,
                    getDrawable(requireContext(), R.drawable.ic_play_button)
                )
                audioPlayBtn.foreground = getDrawable(requireContext(), R.drawable.ic_pause)
                utill.audioRecordUtill.isAudioPlaying = true
            }
        }



        return view
    }

    private fun setCalculatorBtnListeners(view: View) {


        utill.CalculatorUtills().setClickListenersOnButtons(view, this)

    }


    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onPause() {
        utill.audioRecordUtill.stopPlayingAudio()
        utill.audioRecordUtill.stopAudioRecording()
        utill.audioRecordUtill.stopTimer()
        super.onPause()
    }


    /*Function to calculate the expressions using expression builder library*/

    override fun evaluateExpression(string: String, clear: Boolean) {
        if (clear) {

            amountTextTV.append(string)
        } else {
            amountTextTV.append(totalAmount.text)
            amountTextTV.append(string)
            totalAmount.setText("")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMaxAudioRecordDuration() {
        recordBtn.foreground = getDrawable(requireContext(), R.drawable.ic_microphone)
        audioLayout.visibility = View.VISIBLE
        hintRecordText.visibility = View.GONE

        // set audioDuration
        recordDuartionText.text = utill.audioRecordUtill.getAudioDuration() + "s"
    }
}