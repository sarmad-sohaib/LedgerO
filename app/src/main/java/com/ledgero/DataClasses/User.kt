package com.ledgero.DataClasses

import com.ledgero.groupLedger.data.GroupInfo


object User{
  var userPhone:String?=""
        var userID:String?=""
        var userEmail:String?=""
        var userName:String?=""
        var user_total_give:Int=0
        var user_total_take:Int=0
        var total_single_ledgers: Int=0
   private   var user_single_Ledgers: ArrayList<SingleLedgers>?=null
      var user_group_Ledgers: ArrayList<GroupLedgers>?=null


    fun getUserSingleLedgers(): ArrayList<SingleLedgers>? {
        if (user_single_Ledgers==null){
            user_single_Ledgers=ArrayList()
        }
        return user_single_Ledgers

    }
    fun setUserSingleLedger(ledgers:ArrayList<SingleLedgers>){
        this.user_single_Ledgers=ledgers
    }
    fun addNewLedgerInList(ledgers: SingleLedgers){
        if (user_single_Ledgers==null){
            user_single_Ledgers= ArrayList<SingleLedgers>()
        }
        this.user_single_Ledgers!!.add(ledgers)
    }
    fun removeLedgerFromList(ledgers: SingleLedgers){
        if (user_single_Ledgers!=null )
        user_single_Ledgers!!.remove(ledgers)
    }




      fun update_User(user:User){

          this.userID=user.userID
          this.user_single_Ledgers=user.user_single_Ledgers
          this.total_single_ledgers=user.total_single_ledgers
          this.userEmail=user.userEmail
          this.userName=user.userName
          this.userPhone=user.userPhone
          this.user_group_Ledgers=user.user_group_Ledgers
          this.user_total_give=user.user_total_give
          this.user_total_take=user.user_total_take
      }

}