package com.ledgero.DataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class Entries(
    var amount:Float?=null,
    var give_take_flag: Boolean?=null, // true ->get , false -> gave
    var entry_desc: String?=null,
    var entry_title: String?=null,
    var entry_timeStamp: Long?=null,
    var isApproved: Boolean?=false,
    var entryMadeBy_userID: String?=null,
    var entryUID: String?=null,
var requestMode:Int?=0   //1->addRequest , 2->deleteRequest ,3->editRequest 0=nothing

                   ): Parcelable
{


}
