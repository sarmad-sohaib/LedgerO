package com.ledgero.DataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class Entries(
    var amount:Float?=null,
    var give_take_flag: Boolean?=null,
    var entry_desc: String?=null,
    var entry_title: String?=null,
    var entry_timeStamp: Long?=null,

                   ): Parcelable
{


}
