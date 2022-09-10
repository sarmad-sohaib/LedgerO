package com.ledgero.UtillClasses

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.os.postDelayed
import com.ledgero.MainActivity
import com.ledgero.fragments.AddNewEntryDetail
import kotlinx.android.synthetic.main.calculator_layout.view.*
import net.objecthunter.exp4j.ExpressionBuilder
import java.io.File


// we are not using ViewModel for AddNewEntryDetail that is why using Utill class
class Utill_AddNewEntryDetail( var addNewEntryDetail: AddNewEntryDetail) {


    var audioRecordUtill=AudioRecordUtill()
    val randomSuffixForAudioFilePath=  (0..9999999).random()



  inner class CalculatorUtills{


        fun setClickListenersOnButtons(view: View, addNewEntryDetail: AddNewEntryDetail) {

            /*Number Buttons*/

            view.btnDoubleZero.setOnClickListener {
                addNewEntryDetail.evaluateExpression("00", clear = true)
                performEqualOperation(addNewEntryDetail)
            }
            view.btnOne.setOnClickListener {
                addNewEntryDetail.evaluateExpression("1", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnTwo.setOnClickListener {
                addNewEntryDetail.evaluateExpression("2", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnThree.setOnClickListener {
                addNewEntryDetail. evaluateExpression("3", clear = true)
                performEqualOperation(addNewEntryDetail)
            }
            view.btnFour.setOnClickListener {
                addNewEntryDetail.  evaluateExpression("4", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnFive.setOnClickListener {
                addNewEntryDetail. evaluateExpression("5", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnSix.setOnClickListener {
                addNewEntryDetail.evaluateExpression("6", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnSeven.setOnClickListener {
                addNewEntryDetail.evaluateExpression("7", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnEight.setOnClickListener {
                addNewEntryDetail.evaluateExpression("8", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnNine.setOnClickListener {
                addNewEntryDetail.evaluateExpression("9", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnZero.setOnClickListener {
                addNewEntryDetail.evaluateExpression("0", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            /*Operators*/

            view.btnPercent.setOnClickListener {
                addNewEntryDetail.evaluateExpression("/100", clear = true)
                performEqualOperation(addNewEntryDetail)
            }
            view.btnPlus.setOnClickListener {
                addNewEntryDetail.evaluateExpression("+", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnMinus.setOnClickListener {
                addNewEntryDetail.evaluateExpression("-", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnMul.setOnClickListener {
                addNewEntryDetail.evaluateExpression("*", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnDivide.setOnClickListener {
                addNewEntryDetail.evaluateExpression("/", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnDot.setOnClickListener {
                addNewEntryDetail. evaluateExpression(".", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnClear.setOnClickListener {
                addNewEntryDetail.amountTextTV.setText("")
                addNewEntryDetail.totalAmount.setText("")
            }

            view.btnEquals.setOnClickListener {
         performEqualOperation(addNewEntryDetail)
                addNewEntryDetail.amountTextTV.setText(addNewEntryDetail.totalAmount.text)

            }


            view.btnBack.setOnClickListener {
                var text =  addNewEntryDetail.amountTextTV.text.toString()

                if(text.isNotEmpty()) {
                    text= text.dropLast(1)
                    addNewEntryDetail.amountTextTV.setText(text)
                    if (text.isNotEmpty()){
                        performEqualOperation(addNewEntryDetail)
                    }else{

                        addNewEntryDetail.totalAmount.setText( "")
                    }
                }

            }

        }


        private  fun performEqualOperation(addNewEntryDetail:AddNewEntryDetail){
            val text = addNewEntryDetail.amountTextTV.text.toString()

            if (!isExpressionCorrect(text)){

                addNewEntryDetail.totalAmount.setText("Err")
             return
            }

            val expression = ExpressionBuilder(text).build()

            try {
                val result = expression.evaluate()
                val longResult = result.toLong()
                if (result == longResult.toDouble()) {
                    addNewEntryDetail.totalAmount.setText(longResult.toString())
                } else {
                    addNewEntryDetail.totalAmount.setText(result.toString())
                }
            }catch (e:ArithmeticException){
                Toast.makeText(addNewEntryDetail.context, "Wrong Expression! ${e.message}", Toast.LENGTH_LONG).show()
            }

        }

        private fun isExpressionCorrect(text: String): Boolean {

            if (text.isEmpty()){
                return false
            }

            var flag= !(text.last().equals('+') || text.last().equals('-') || text.last().equals('/') || text.last().equals('*') || text.last().equals('.'))

            return flag
        }
    }

    inner class AudioRecordUtill{
        private val AUDIO_PERMISSON_CODE = 200
        private var isPermissionGranted= false
        var packageManager=MainActivity.getMainActivityInstance().packageManager
        var context=MainActivity.getMainActivityInstance().applicationContext
        lateinit var handler:Handler
        var hasVoiceNote=false
        var localPath:String?=null




        var mediaPlayer:MediaPlayer?=null
        @RequiresApi(Build.VERSION_CODES.S)
         var mediaRecorder:MediaRecorder?=null
         var isAudioRecording= false
         var isAudioPlaying= false




        @RequiresApi(Build.VERSION_CODES.S)
        fun startAudioRecording(){
           mediaRecorder = MediaRecorder()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder!!.setOutputFile(getPathForStoringFile())
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setMaxDuration(60*1000)
            mediaRecorder!!.prepare()
            mediaRecorder!!.setOnInfoListener(object :MediaRecorder.OnInfoListener{
                override fun onInfo(p0: MediaRecorder?, p1: Int, p2: Int) {

                    if (p1==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                        isAudioRecording=false
                        stopAudioRecording()
                        addNewEntryDetail.onMaxAudioRecordDuration()
                    }

                }

            })

            mediaRecorder!!.start()

            Toast.makeText(context, "Recording Started", Toast.LENGTH_SHORT).show()
            }

       var timer:CountDownTimer?=null
            fun startTimer(view:TextView){
                timer = object: CountDownTimer(60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                       var time= ((millisUntilFinished/1000)-60)* -1
                        view.setText("$time/60")
                    }

                    override fun onFinish() {
                        timer!!.cancel()
                        view.text="Max 60s"

                    }
                }
                timer!!.start()
            }
        fun stopTimer(){
            if (timer!=null){
                timer!!.cancel()
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun stopAudioRecording(){

            try {


            if (mediaRecorder==null){
                return
            }
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder=null
            Toast.makeText(context, "Recording Stopped", Toast.LENGTH_SHORT).show()
            }catch (e:Exception){
                Log.d("Utill_AddNewEntryDetail", "stopAudioRecording: ${e.message}")

                e.printStackTrace()
            }
        }
        @RequiresApi(Build.VERSION_CODES.M)
        fun startPlayingAudio(audioPlayBtn: Button, drawable: Drawable?) {

            mediaPlayer= MediaPlayer()

    mediaPlayer!!.setOnCompletionListener(){
    audioPlayBtn.foreground=drawable
    isAudioPlaying=false
    stopPlayingAudio() }

            mediaPlayer!!.setDataSource(getPathForStoringFile())
            mediaPlayer!!.prepare()
            setUpSeekBar(addNewEntryDetail.recordSeekBar)
            makeHandler(addNewEntryDetail.recordSeekBar)

            mediaPlayer!!.start()

        }
        fun stopPlayingAudio(){
           if (mediaPlayer != null) {

               mediaPlayer!!.stop()
                addNewEntryDetail.recordSeekBar.progress=0
               mediaPlayer!!.release()
               mediaPlayer=null
                }


        }

        fun getAudioDuration():String{
            mediaPlayer= MediaPlayer()
            mediaPlayer!!.setDataSource(getPathForStoringFile())
            mediaPlayer!!.prepare()
          var duartion=  (mediaPlayer!!.duration/1000).toString()
            return duartion
        }

        fun isMicPresent():Boolean{
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE))
            {

                return true
            }

            return false
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun isMicPermission():Boolean{

            //checking if we don't have permission
            if (MainActivity.getMainActivityInstance().
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_DENIED){

                return getMicPermission()
            }
            return true
      }
        @RequiresApi(Build.VERSION_CODES.M)
        fun getMicPermission():Boolean{
            val permission= arrayOf(Manifest.permission.RECORD_AUDIO)

            //asking for permission
            ActivityCompat.requestPermissions(MainActivity.getMainActivityInstance(), permission,AUDIO_PERMISSON_CODE)

            //checking if we don't have permission
            if (MainActivity.getMainActivityInstance().
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_DENIED){

                return false
            }
            return true

        }

        fun getAlertDialogForDeletingVoice(): android.app.AlertDialog.Builder {

            var dialog= android.app.AlertDialog.Builder(addNewEntryDetail.context)

            dialog.setTitle("Deleting Voice Note")
                .setMessage("Are you sure to delete Voice Note!")
                .setCancelable(true)
                .setPositiveButton("Yes Delete it"){dialogInterface,it->
                    //delete voice
                    val fdelete: File = File(getPathForStoringFile())
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            if (isAudioPlaying){
                                stopPlayingAudio()
                            }
                            hasVoiceNote=false
                            localPath=null
                            addNewEntryDetail.audioLayout.visibility=View.GONE
                            addNewEntryDetail.hintRecordText.text="Tap mic button to start recording voice message for entry"
                            addNewEntryDetail.hintRecordText.visibility= View.VISIBLE
                        } else {

                            Toast.makeText(addNewEntryDetail.context, "Sorry! Not Able To Delete Voice Note", Toast.LENGTH_SHORT).show()
                        }
                    }
                    }

                .setNegativeButton("No"){dialogInterface,it->
                    //cancel it
                    dialogInterface.cancel()


                }
            return dialog
        }
        fun setUpSeekBar(mSeekBar:SeekBar){
            mSeekBar.setMax(mediaPlayer!!.duration)
            mSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (mediaPlayer != null && fromUser) {
                        mediaPlayer!!.seekTo(progress)
                    }
                }
            })

        }
        private fun makeHandler(mSeekBar: SeekBar) {
           handler= object : Handler(){ }

            var runnable=object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        Log.d("Utill_AddNewEntry", "run: runnable for seekbar")
                        val mCurrentPosition: Int = mediaPlayer!!.currentPosition
                        mSeekBar.setProgress(mCurrentPosition)
                        handler.postDelayed(this, 100)
                    }
                }
            }

            handler.postDelayed(runnable,100)

        }

    private fun getPathForStoringFile():String{
       var contextWrapper= ContextWrapper(context.applicationContext)
        var voiceDirectory: File = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
        var fileName= addNewEntryDetail.ledger.ledgerUID.toString()+addNewEntryDetail.ledger.entries!!.size+"$randomSuffixForAudioFilePath"+".mp3"
        var file= File(voiceDirectory,fileName)
        hasVoiceNote=true
        localPath=file.path




       if(Build.VERSION.SDK_INT < 26) {
           return file.absolutePath}
       else{return file.path                }

    }





    }



    }

