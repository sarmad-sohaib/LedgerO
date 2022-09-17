package com.ledgero.DataClasses

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList
@Parcelize
data class SingleLedgers(    var friend_userID:String?=null,
                             var friend_userName:String?=null,
                             var friend_userEmail:String?=null,
                             var total_entries:Int?=0,
                             var total_amount:Float?=0.0f,
                             var ledger_Created_timeStamp: Long?=null,
                             var entries:ArrayList<Entries>?=null,
                             var give_take_flag:Boolean?=false, // null means all clear /false means he owe/get//You'll Give money, true mean he lend/gave/You'll Get
                             var reminder_time: Date?=null,
                             var ledgerUID:String?=null,
                             var ledgerCreatedByUID:String?=""



): Parcelable
{
    @Exclude
    fun addEntry(value:Entries){
        if (entries.isNullOrEmpty()){
            entries= ArrayList<Entries>()
        }
        entries!!.add(0,value)
    }
    @Exclude
    fun getAllEntries(): ArrayList<Entries>{
        if (entries.isNullOrEmpty()){
            entries= ArrayList<Entries>()
        }
        return entries!!
    }
}