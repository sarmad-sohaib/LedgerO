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
                             var total_entries:Int?=null,
                             var total_amount:Float?=null,
                             var entries:ArrayList<Entries>?=null,
                             var ledger_Created_timeStamp: Date?=null,
                             var give_take_flag:Boolean?=null,
                             var reminder_time: Date?=null,
                             var entriesListUID:String?=null,
                             var ledgerUID:String?=null


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