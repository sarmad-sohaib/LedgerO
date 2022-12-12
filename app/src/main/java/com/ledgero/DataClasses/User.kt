package com.ledgero.DataClasses

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.groupLedger.data.GroupInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.coroutineContext


object User {
    var userPhone: String? = ""
    var userID: String? = ""
    var userEmail: String? = ""
    var userName: String? = ""
    var user_total_give: Int = 0
    var user_total_take: Int = 0
    var total_single_ledgers: Int = 0
    private var user_single_Ledgers: ArrayList<SingleLedgers>? = null
    var user_group_Ledgers: ArrayList<GroupLedgers>? = null



    fun getUserSingleLedgers(): ArrayList<SingleLedgers>? {
        if (user_single_Ledgers == null) {
            user_single_Ledgers = ArrayList()
        }
        return user_single_Ledgers

    }

    fun setUserSingleLedger(ledgers: ArrayList<SingleLedgers>) {
        this.user_single_Ledgers = ledgers
    }

    fun addNewLedgerInList(ledgers: SingleLedgers) {
        if (user_single_Ledgers == null) {
            user_single_Ledgers = ArrayList<SingleLedgers>()
        }
        this.user_single_Ledgers!!.add(ledgers)
    }

    fun removeLedgerFromList(ledgers: SingleLedgers) {
        if (user_single_Ledgers != null){
            for (i in 0 until user_single_Ledgers!!.size){
                if (user_single_Ledgers!![i].ledgerUID== ledgers.ledgerUID){
                    user_single_Ledgers!!.removeAt(i)
                }
            }
        }
    }


    fun update_User(user: User) {

        this.userID = user.userID
        this.user_single_Ledgers = user.user_single_Ledgers
        this.total_single_ledgers = user.total_single_ledgers
        this.userEmail = user.userEmail
        this.userName = user.userName
        this.userPhone = user.userPhone
        this.user_group_Ledgers = user.user_group_Ledgers
        this.user_total_give = user.user_total_give
        this.user_total_take = user.user_total_take
    }

    fun signOut() {
        userName=null
        userID=null

        userEmail= null
        userName = null
        user_total_give= 0
        user_total_take=0
        total_single_ledgers=0
        user_single_Ledgers=null
        user_group_Ledgers=null
    }


}