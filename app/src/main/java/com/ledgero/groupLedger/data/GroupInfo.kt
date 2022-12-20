package com.ledgero.groupLedger.data

import android.os.Parcelable
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.other.Constants.CASH_IN
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupInfo(
    var groupName:String="",
    var groupUID:String="",
    var groupAdminUID:String="",
    var groupTotalMembers:Int=0,
    var groupMembers:ArrayList<Member>?=null,
    var groupTotalAmount:Float=0f,
    val groupCreateServerTimestamp: String="",
    var groupAdminName:String="",
    var groupAdminEmail:String="",
    var cashInOut:Boolean= CASH_IN
        ): Parcelable