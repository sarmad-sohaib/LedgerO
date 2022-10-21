package com.ledgero.ViewModels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ledgero.DataClasses.Entries
import com.ledgero.MainActivity
import com.ledgero.Repositories.IndividualScreenRepo
import java.io.File


class IndividualScreenViewModel(private val individualScreenRepo: IndividualScreenRepo) : ViewModel() {

    private var allEntries: LiveData<ArrayList<Entries>>
    private var TAG= "IndividualScreenVM"
    private var totalAmount:LiveData<Float>
    private var giveTakeFlag:LiveData<Boolean?>
    private val vibrator = initVibrator()



    private fun initVibrator(): Vibrator {
        val vib= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = MainActivity.mainActivity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            MainActivity.mainActivity.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        return vib
    }


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


        val fdelete= File( contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.toString()+"/"+entry.voiceNote!!.fileName
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


   fun startListeningForUnApprovedEntriesCount():LiveData<Long>{

   return individualScreenRepo.startListeningForUnApprovedEntries()
    }
    fun stopListeningForUnApprovedEntriesCount(){
    individualScreenRepo.stopListeningForUnApprovedEntries()
    }

    fun vibratePhoneForNewUnApprovedEntry(){
        when {
            ContextCompat.checkSelfPermission(
                MainActivity.mainActivity.applicationContext,
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                var  vibrationEffect : VibrationEffect

                // this type of vibration requires API 29
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                    // create vibrator effect with the constant EFFECT_TICK
                    vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)

                    // it is safe to cancel other vibrations currently taking place
                    vibrator.cancel()

                    vibrator.vibrate(vibrationEffect)
                }else{
                // this type of vibration requires API 26
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            {


                // this effect creates the vibration of default amplitude for 1000ms(1 sec)
                vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)

                // it is safe to cancel other vibrations currently taking place
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)
            }}}
            else -> {
                // You can directly ask for the permission.
                requestPermissions(MainActivity.mainActivity.applicationContext as Activity,
                    arrayOf(Manifest.permission.VIBRATE),
                    22)
            }
        }


    }
    fun startListeningForCancelledEntries(): LiveData<Long> {


        return individualScreenRepo.startListeningForCancelledEntries()

    }

    fun stopListeningForCancelledEntries() {

        individualScreenRepo.stopListeningForCancelledEntries()
    }
}