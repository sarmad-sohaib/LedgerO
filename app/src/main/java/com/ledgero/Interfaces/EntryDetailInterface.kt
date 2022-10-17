package com.ledgero.Interfaces

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ledgero.DataClasses.SingleLedgers

interface EntryDetailInterface {
    var calculatorLayout: View
    var amountTextTV:EditText
    var totalAmount:EditText
    var myContext: Context
    var recordSeekBar: SeekBar
    var audioLayout: ConstraintLayout
    var audioPlayBtn: Button
    var recordBtn : Button
    var recordDuartionText: TextView
    var recordMaxLimitText: TextView
    var hintRecordText: TextView
    var mLedger: SingleLedgers

    fun onMaxAudioRecordDuration()
    fun evaluateExpression(string: String, clear: Boolean)
}