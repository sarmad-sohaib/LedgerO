package com.ledgero.DataClasses

import android.os.Parcelable
import com.google.firebase.database.ServerValue
import com.ledgero.other.Constants.NO_REQUEST_REQUEST_MODE
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
@Parcelize
open   class Entries(
    var amount:Float?=null,
    var give_take_flag: Boolean?=null,  // null means all clear /false means he owe/get//You'll Give money, true mean he lend/gave/You'll Get
    var entry_desc: String?=null,
    var entry_title: String?=null,
    var entry_timeStamp: Long?=null,
    var isApproved: Boolean?=false,
    var entryMadeBy_userID: String?=null,
    var entryUID: String?=null,
    var requestMode:Int?=NO_REQUEST_REQUEST_MODE,  //1->addRequest , 2->deleteRequest ,3->editRequest 0=nothing
    var hasVoiceNote: Boolean?=false,
    var voiceNote: VoiceNote?=null,
    var originally_addedByUID:String?=null //we will use this to calculate amount when this entry has to be deleted from approved ledger
                   ): Parcelable
