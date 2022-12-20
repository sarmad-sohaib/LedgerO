package com.ledgero.fragments

import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ledgero.daos.ViewEntryInfoScreenDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.Repositories.ViewEntryInfoScreenRepo
import com.ledgero.ViewModelFactories.ViewEntryInfoScreenViewModelFactory
import com.ledgero.ViewModels.ViewEntryInfoScreenViewModel
import com.ledgero.groupLedger.data.GroupInfo
import kotlinx.android.synthetic.main.fragment_view_entry_info_screen.view.*

class ViewEntryInfoScreen(val entry:Entries,val ledgerUID:String,var isUnapproved:Boolean=false, val groupInfo: GroupInfo?=null) : Fragment() {

        lateinit var viewModel: ViewEntryInfoScreenViewModel
    lateinit var totalAmount: EditText
    lateinit var description: EditText
    lateinit var voiceLayout: ConstraintLayout
    lateinit var voicePlayButton: Button
    lateinit var seekBar: SeekBar
    lateinit var voiceRecordButton: Button
    lateinit var voiceDuration: TextView
    lateinit var hintVoiceNoteDownload:TextView

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_view_entry_info_screen, container, false)


        var dao= ViewEntryInfoScreenDAO(ledgerUID)
        var repo = ViewEntryInfoScreenRepo(dao)
        viewModel= ViewModelProvider(this, ViewEntryInfoScreenViewModelFactory(repo))
            .get(ViewEntryInfoScreenViewModel::class.java)
        viewModel.view=this

            totalAmount= view.tv_Totalamount_view_entry
            try {
                view.add_new_entry_Totalamount_filledTextField.hint= (if(entry.give_take_flag!!){"You Take"}else{"You Gave"}).toString()

            }catch (e:Exception){
                Log.d("ViewEntry", "onCreateView: ${e.message}")
            }

            description= view.tv_description_view_entry

            totalAmount.setText( entry.amount.toString())
            description.setText(entry.entry_desc)

            if (entry.hasVoiceNote!!){

                hintVoiceNoteDownload=view.hint_downloadingAudio_view_entry
                voiceLayout=view.audioPlay_layout_view_entry
                viewModel.readyVoiceNoteToPlay(entry)
                voicePlayButton= view.btn_play_recordVoice_view_entry
                seekBar= view.seekBar_view_entry
                voiceDuration= view.audioRecordDuration_tv_view_entry
                voiceDuration.text=entry.voiceNote!!.duration.toString()+"s"



                voicePlayButton.setOnClickListener {
                    if (viewModel.isAudioPlaying){
                        viewModel.stopPlayingAudio()
                    }else{
                        viewModel.startPlayingAudio()
                    }
                }

            }



        view.bt_edit_view_entry.setOnClickListener(){

            if (groupInfo!= null){
                val frag = EditEntrySingleScreen("null",entry,groupInfo)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fl_fragment_container_main,frag)
                    .addToBackStack(null)
                    .commit()
            }else{

            val frag = EditEntrySingleScreen(ledgerUID,entry,null)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_fragment_container_main,frag)
                .addToBackStack(null)
                .commit()
            }
            }


            //approve entry
            if (isUnapproved){
                view.bt_edit_view_entry.visibility= View.GONE
            }
            //if not group admin
            if (groupInfo!=null){

                if (groupInfo.groupAdminUID!= User.userID){
                    view.bt_edit_view_entry.visibility= View.GONE
                }
            }


        return view
    }

fun observeDownload(dataDownload: LiveData<Int>) {
      hintVoiceNoteDownload.visibility=View.VISIBLE
        dataDownload.observe(viewLifecycleOwner, Observer{

            hintVoiceNoteDownload.text="Voice note is downloading: $it%"
if (it>=100){
    hintVoiceNoteDownload.visibility=View.GONE
    voiceLayout.visibility=View.VISIBLE
}

        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPause() {
       if (viewModel.isAudioPlaying){
           viewModel.stopPlayingAudio()
       }

        super.onPause()
    }
}