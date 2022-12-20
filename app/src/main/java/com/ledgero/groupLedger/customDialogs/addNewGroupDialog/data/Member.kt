package com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Member (
    var name:String="",
    var email:String="",
    var uid:String=""
        ): Parcelable