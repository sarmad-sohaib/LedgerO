package com.ledgero.DataClasses

import com.ledgero.Interfaces.OnUpdateUserSingleLedger
import com.ledgero.model.DatabaseUtill

object User{

      var userPhone:String?=""
    var userID:String?=""
        var userEmail:String?=""
         var userName:String?=""
      var user_single_Ledgers: ArrayList<SingleLedgers>?=null
      var user_group_Ledgers: ArrayList<GroupLedgers>?=null
      var user_total_give:Int=0
      var user_total_take:Int=0
      var total_single_ledgers: Int=0



// this function can be used when we want
// add a new ledger by adding a new friend
//so we can just make a empty ledger with default
//values and add given friend ledger
fun add_friend_for_single_ledger(friend:FriendUsers){

    if (user_single_Ledgers==null){
        user_single_Ledgers= ArrayList()
    }
    // going to create a new single ledger because user wants to start
    //a ledger with given friend, but we dont have any data to add as
    //entries, so i will create an empty ledger under the name of given
    //friend and then user's ledger with this friend with will open
    //and user can start adding entries

    var single_ledger= SingleLedgers()
    single_ledger.friend_user=friend
    single_ledger.ledgerUID= userID+friend.userID
    this.user_single_Ledgers!!.add(single_ledger)
    var db=DatabaseUtill()
    db.createNewSingleLedger(this.userID!!, user_single_Ledgers!!,object : OnUpdateUserSingleLedger{

        override fun onSingleLedgerUpdated(boolean: Boolean) {

        }
    })


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