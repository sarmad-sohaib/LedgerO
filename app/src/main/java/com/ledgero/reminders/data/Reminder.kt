package com.ledgero.reminders.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger
import java.util.*

@Parcelize
data class Reminder(
    val amount: String? = null,
    val recipient: String? = null,
    val description: String? = null,
    val timeStamp: Long? = 0L,
    val complete: Boolean? = false,
    val give: Boolean? = false,
    val id: String = String.format("%010d", BigInteger(UUID.randomUUID().toString().replace("-", ""), 16))
) : Parcelable
