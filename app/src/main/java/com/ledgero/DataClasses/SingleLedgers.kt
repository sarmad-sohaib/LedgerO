package com.ledgero.DataClasses

import java.util.*
import kotlin.collections.ArrayList

class SingleLedgers {

   var friendName:String?=null
   var friendUID:String?=null
   var friendEmail:String?=null
   var total_entries:Int?=null
   var total_amount:Float?=null
  var entries:ArrayList<Entries>?=null
  var ledger_Created_timeStamp: Date?=null
  var give_take_flag:Boolean?=null
  var reminder_time: Date?=null
}