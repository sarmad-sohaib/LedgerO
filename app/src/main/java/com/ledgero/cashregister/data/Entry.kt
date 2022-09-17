package com.ledgero.cashregister.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.util.*

@Parcelize
data class Entry (
    val amount: String? = null,
    val description: String? = null,
    val out: Boolean? = null,
    var dateTimeStamp: Long? = null,
    var id: String? = UUID.randomUUID().toString() //creating unique id each time an entry is created
): Parcelable {

    init {
        dateTimeStamp = System.currentTimeMillis()
    }
}
