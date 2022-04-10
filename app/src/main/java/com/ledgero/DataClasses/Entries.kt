package com.ledgero.DataClasses

import java.util.*

data class Entries(
    var amount:Float,
    var give_take_flag: Boolean,
    var entry_desc: String,
    var entry_title: String,
    var entry_timeStamp: Long,
    var friendName:String,
    var total_amount:Float
                   )
