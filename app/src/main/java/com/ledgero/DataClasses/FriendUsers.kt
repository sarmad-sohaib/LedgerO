package com.ledgero.DataClasses

data class FriendUsers(  var userPhone:String?="",
                         var userID:String?="",
                           var userEmail:String?="",
                           var userName:String?="",
                       var user_single_Ledgers: ArrayList<SingleLedgers>?=null,
                       var user_group_Ledgers: ArrayList<GroupLedgers>?=null,
                       var user_total_give:Int?=0,
                       var user_total_take:Int?=0,
                       var total_single_ledgers: Int?=0
                )
