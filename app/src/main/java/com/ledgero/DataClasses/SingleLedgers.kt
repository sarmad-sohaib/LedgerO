package com.ledgero.DataClasses

import java.util.*
import kotlin.collections.ArrayList

data class SingleLedgers(    var friend_user:FriendUsers?=null,
                             var total_entries:Int?=null,
                             var total_amount:Float?=null,
                             var entries:ArrayList<Entries>?=null,
                             var ledger_Created_timeStamp: Date?=null,
                             var give_take_flag:Boolean?=null,
                             var reminder_time: Date?=null,
                             var entriesListUID:String?=null,
                             var ledgerUID:String?=null
)