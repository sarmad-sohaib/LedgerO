package com.ledgero.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ledgero.daos.EditSingleEntriesDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.EntryDetailInterface
import com.ledgero.R
import com.ledgero.Repositories.EditSingleEntriesRepo
import com.ledgero.utils.Utill_AddNewEntryDetail
import com.ledgero.ViewModelFactories.EditSingleEntriesViewModelFactory
import com.ledgero.ViewModels.EditSingleEntriesViewModel
import com.ledgero.other.Constants
import com.ledgero.pushnotifications.PushNotification
import kotlinx.android.synthetic.main.fragment_add_new_entry_detail.view.*
import kotlinx.android.synthetic.main.fragment_edit_entry_single_screen.view.*
import kotlinx.android.synthetic.main.fragment_money.view.*

class EditEntrySingleScreen(private val currentLedger:String, private val currentEntry:Entries) : Fragment() , EntryDetailInterface {

    override lateinit var calculatorLayout: View
    override lateinit var amountTextTV: EditText
    override lateinit var totalAmount: EditText
    override lateinit var myContext: Context
    override lateinit var recordSeekBar: SeekBar
    override lateinit var audioLayout: ConstraintLayout
    override lateinit var audioPlayBtn: Button
    override lateinit var recordBtn: Button
    override lateinit var recordDuartionText: TextView
    override lateinit var recordMaxLimitText: TextView
    override lateinit var hintRecordText: TextView
    override lateinit var mLedger: SingleLedgers
  lateinit  var saveButton:Button

    lateinit var utill: Utill_AddNewEntryDetail
    private val pushNotificationInterface= PushNotification()


    lateinit var viewModel: EditSingleEntriesViewModel

  lateinit  var progressDialog:AlertDialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_edit_entry_single_screen, container, false)

        calculatorLayout = view.calculator_layout_edit_entry_include
        amountTextTV= view.tv_amount_edit_entry
        totalAmount= view.tv_Totalamount_edit_entry
        myContext=requireContext()
        recordSeekBar=view.seekBar_editEntry
        audioLayout=view.audioPlay_layout_editEntry
        audioPlayBtn= view.btn_play_recordVoice_editEntryDetail
        recordBtn=view.btn_recordVoice_editEntry
        recordDuartionText = view.audioRecordDuration_tv_editEntryDetail
        recordMaxLimitText=view.audioMaxLimit_tv_editEntryDetail
        hintRecordText=view.hint_recordAudio_editEntry
        saveButton=view.bt_save_edit_entry
     for ( i in User.getUserSingleLedgers()!!) {if(i.ledgerUID.equals(currentLedger))mLedger=i}



        var dao= EditSingleEntriesDAO(currentLedger)
        var repo = EditSingleEntriesRepo(dao)
        viewModel= ViewModelProvider(this, EditSingleEntriesViewModelFactory(repo,currentEntry))
            .get(EditSingleEntriesViewModel::class.java)

        viewModel.context=requireContext()
        viewModel.setProgressDialog("Sending Request For Approval")
        utill= Utill_AddNewEntryDetail(this)


        totalAmount.setText(currentEntry.amount.toString())
        view.tv_description_edit_entry.setText(currentEntry.entry_desc)
        if (currentEntry.hasVoiceNote!!){
            utill.audioRecordUtill.hasVoiceNote= currentEntry.hasVoiceNote!!
            utill.audioRecordUtill.localPath=currentEntry.voiceNote!!.localPath
            utill.audioRecordUtill.VoicefileName=currentEntry.voiceNote!!.fileName
            audioLayout.visibility=View.VISIBLE
            hintRecordText.visibility= View.GONE
        }




        setCalculatorBtnListeners(view)


        amountTextTV.setInputType(InputType.TYPE_NULL) // disable soft input



        amountTextTV.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                //this will close the soft keyboard if its open
                // hide virtual keyboard
                hideKeyboard()
                if (view.calculator_layout_edit_entry_include.visibility==View.GONE){
                    view.calculator_layout_edit_entry_include.visibility=View.VISIBLE

                }

                return false
            }})





        view.tv_description_edit_entry.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                view.calculator_layout_edit_entry_include.visibility=View.GONE
                return false

            }

        })


        amountTextTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                ;
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                ;
            }

            override fun afterTextChanged(p0: Editable?) {
                var amount= if(view.tv_amount_edit_entry.text.isNullOrEmpty()) 0f else view.tv_amount_edit_entry.text.toString().toFloat()
                if ( amount  <=0f){
                    view.entryInfoScrollView_edit_entry_screen.visibility=View.GONE
                saveButton.isEnabled= false
                    saveButton.text="Total Amount is Empty"
                }else{
                    view.entryInfoScrollView_edit_entry_screen.visibility=View.VISIBLE
                    saveButton.isEnabled= true

                    saveButton.text="Save"

                }
            }

        })

        saveButton.setOnClickListener(){


            if (view.tv_description_edit_entry.text.isNullOrEmpty()){
                Toast.makeText(context, "Please Add Description", Toast.LENGTH_SHORT).show()

            }else{

                if (totalAmount.text.toString().toFloat()== currentEntry.amount &&
                    view.tv_description_edit_entry.text!!.toString().equals(currentEntry.entry_desc,true) && !viewModel.isAudioUpdated
                        ){

                    Toast.makeText(context, "No Changes...", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()

                }
                else{
                    Toast.makeText(context, "Sent For Approval", Toast.LENGTH_SHORT).show()

                    viewModel.addEntryInUnApproved_ForEdit(view.tv_description_edit_entry.text.toString(),totalAmount.text.toString().toFloat())

                    pushNotificationInterface.createAndSendNotification(currentLedger,
                        Constants.EDIT_REQUEST_REQUEST_MODE)
                }

            }
        }


        audioPlayBtn.setOnClickListener(){

            if (utill.audioRecordUtill.isAudioPlaying){
                utill.audioRecordUtill.stopPlayingAudio()
                audioPlayBtn.foreground=
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_button)
                utill.audioRecordUtill.isAudioPlaying=false
            }else{
                utill.audioRecordUtill.startPlayingAudio(audioPlayBtn,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_button))
                audioPlayBtn.foreground=
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause)
                utill.audioRecordUtill.isAudioPlaying=true
            }
        }


        audioLayout.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {

                var alertDialog=  android.app.AlertDialog.Builder(context)

                alertDialog.setTitle("Deleting Voice Note")
                    .setMessage("Are you sure to delete Voice Note!")
                    .setCancelable(true)
                    .setPositiveButton("Yes Delete it"){dialogInterface,it->
                        //delete voice
                        viewModel.deleteAudio()
                        hideAudioLayoutAndShowHint()

                    }

                    .setNegativeButton("No") { dialogInterface, it ->
                        //cancel it
                        dialogInterface.cancel()
                    }

                alertDialog.show()
                return true
            }

        })


        recordBtn.setOnClickListener(){
            @RequiresApi(Build.VERSION_CODES.S)
            if (utill.audioRecordUtill.isAudioRecording){

                //stop audio Recording
                utill.audioRecordUtill.stopAudioRecording()
                utill.audioRecordUtill.stopTimer()
                utill.audioRecordUtill.isAudioRecording=false
                recordBtn.foreground=
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_microphone)
                audioLayout.visibility = View.VISIBLE
                hintRecordText.visibility=View.GONE
                recordMaxLimitText.text="Max 60s"
                viewModel.audioPath= utill.audioRecordUtill.voiceNewVersionPath.toString()

                // set audioDuration
                view.audioRecordDuration_tv_editEntryDetail.text=utill.audioRecordUtill.getAudioDuration()+"s"
            }else
            {
                //start audio Recording
                if (utill.audioRecordUtill.isMicPresent()) {
                    if (!utill.audioRecordUtill.isMicPermission())
                    {Toast.makeText(context,"Please Allow Us To Use Your Device Mice", Toast.LENGTH_SHORT).show()
                    }else{
                        utill.audioRecordUtill.isEditingVoiceNote=true
                        utill.audioRecordUtill.startAudioRecording()
                        viewModel.hasAudio=true
                        viewModel.isAudioUpdated=true
                        viewModel.isAudioDeleted=false
                        utill.audioRecordUtill.startTimer(recordMaxLimitText)
                        utill.audioRecordUtill.isAudioRecording=true
                        audioLayout.visibility=View.GONE
                        hintRecordText.text= "Your voice note is recording. Tap recording button to stop"
                        hintRecordText.visibility=View.VISIBLE

                        recordBtn.foreground= ContextCompat.getDrawable(requireContext(),
                            R.drawable.ic_sound_waveform)
                    } } else {
                    Toast.makeText(context, "Sorry! No Working Mic Found", Toast.LENGTH_SHORT)
                        .show() }
            }
        }



        return view
    }

    private fun hideAudioLayoutAndShowHint() {

        audioLayout.visibility=View.GONE
        hintRecordText.visibility=View.VISIBLE
    }


    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onPause() {
        utill.audioRecordUtill.stopPlayingAudio()
        utill.audioRecordUtill.stopAudioRecording()
        utill.audioRecordUtill.stopTimer()
        super.onPause()
    }
    override fun onStop() {

        super.onStop()

    }


    /*Function to calculate the expressions using expression builder library*/

    override fun evaluateExpression(string: String, clear: Boolean) {
        if(clear) {

            amountTextTV.append(string)
        } else {
            amountTextTV.append(totalAmount.text)
            amountTextTV.append(string)
            totalAmount.setText("")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMaxAudioRecordDuration(){
        recordBtn.foreground= ContextCompat.getDrawable(requireContext(), R.drawable.ic_microphone)
        audioLayout.visibility = View.VISIBLE
        hintRecordText.visibility= View.GONE

        // set audioDuration
        recordDuartionText.setText(utill.audioRecordUtill.getAudioDuration()+"s")
    }

    private fun setCalculatorBtnListeners(view: View) {


        utill.CalculatorUtills().setClickListenersOnButtons(view,this)

    }

}