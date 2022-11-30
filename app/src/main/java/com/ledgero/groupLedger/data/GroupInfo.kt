package com.ledgero.groupLedger.data

import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member

data class GroupInfo(
    var groupName:String="",
    var groupUID:String="",
    var groupAdminUID:String="",
    var groupTotalMembers:Int=0,
    var groupMembers:ArrayList<Member>?,
    var groupTotalAmount:Float=0f,
    val groupCreateServerTimestamp: String=""
        )