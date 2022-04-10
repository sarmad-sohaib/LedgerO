package com.ledgero.DataClasses

data class User(
    var userPhone:String? = null,
    var userID:String?= null,
    var userEmail:String?= null,
    var userName:String?= null,
    var user_single_Ledgers: List<SingleLedgers>?=null,
    var user_group_Ledgers: List<GroupLedgers>?=null,
    var user_total_give:Int?=null,
    var user_total_take:Int?=null,
var total_single_ledgers: Int?=null

)