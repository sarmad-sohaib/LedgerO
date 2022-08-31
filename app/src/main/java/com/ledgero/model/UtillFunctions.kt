package com.ledgero.model

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.OnUpdateUserSingleLedger
import com.ledgero.Interfaces.OnUserDetailUpdate
import com.ledgero.fragments.LedgersFragment


//all operations to do on client side
class UtillFunctions {

    val TAG="UtillFunctions"
    companion object{


        // this function can be used when we want
// add a new ledger by adding a new friend
//so we can just make a empty ledger with default
//values and add given friend ledger
        fun createNewLedger(friendUID: String, friendUserEmail: String, friendUserName: String){

            if (User.getUserSingleLedgers() ==null){
                User.setUserSingleLedger( ArrayList())
            }
            // going to create a new single ledger because user wants to start
            //a ledger with given friend, but we dont have any data to add as
            //entries, so i will create an empty ledger under the name of given
            //friend and then user's ledger with this friend with will open
            //and user can start adding entries

            var single_ledger= SingleLedgers()
            single_ledger.friend_userID=friendUID
            single_ledger.friend_userEmail=friendUserEmail
            single_ledger.friend_userName=friendUserName
            single_ledger.ledgerUID= User.userID +friendUID
            User.addNewLedgerInList(single_ledger)
            LedgersFragment.adapter?.notifyDataSetChanged()
            var db=DatabaseUtill()
            db.createNewSingleLedger(User.userID!!, single_ledger, User.getUserSingleLedgers()!!, object :
                OnUpdateUserSingleLedger {

                override fun onSingleLedgerUpdated(boolean: Boolean) {

                    if (boolean){

                        //adding this new ledger into friend uid too

                        var single_ledgerForFriend= SingleLedgers()
                        single_ledgerForFriend.friend_userID=User.userID
                        single_ledgerForFriend.friend_userEmail=User.userEmail
                        single_ledgerForFriend.friend_userName=User.userName
                        single_ledgerForFriend.ledgerUID= User.userID +friendUID
                            db.sendLedgerRequestToFriend(friendUID,single_ledgerForFriend,object :OnUserDetailUpdate{
                                override fun onUserDetailsUpdated(boolean: Boolean) {

                                    if (boolean){
                                        Log.d("UtillFunctions", "onUserDetailsUpdated: friend Accepted Your Ledger Request")
                                    }else{
                                        Log.d("UtillFunctions", "onUserDetailsUpdated: error in friend Request")
                                    }
                                }

                            })

                        //updating total ledgernumber for this user in firebase
                        if (User.getUserSingleLedgers()!=null){
                            User.total_single_ledgers = User.getUserSingleLedgers()!!.size
                        }else{
                            User.total_single_ledgers = 0

                        }
                        db.updateUserTotalLedgerCount(User.userID!!,object:OnUserDetailUpdate{
                            override fun onUserDetailsUpdated(boolean: Boolean) {
                                if (boolean){
                                    Log.d("User", "onUserDetailsUpdated: updated")
                                }
                            }


                        })
                    }
                }
            })



        }


        fun removeSingleUserLedgers(user_single_Ledgers: SingleLedgers){


         User.removeLedgerFromList(user_single_Ledgers)

            LedgersFragment.adapter!!!!.notifyDataSetChanged()

            User.total_single_ledgers= User.getUserSingleLedgers()!!.size


            DatabaseUtill().updateUserTotalLedgerCount(User.userID!!,object :OnUserDetailUpdate{
                override fun onUserDetailsUpdated(boolean: Boolean) {
                    if (boolean)
                        Log.d("UtillFunctions", "onUserDetailsUpdated: total number of ledgers updated ")

                }

            })
        }


        /*
        it will be called by firebase listener when some other user add this user as new ledger
         firebase listener will detect a new ledger added in this user firebase
        then it will call this function add will pass the new ledger added
        so we add this newly added ledger into this user list*/

        fun addNewSingleUserLedgers(user_single_Ledgers: SingleLedgers){
            User.addNewLedgerInList(user_single_Ledgers)
            User.total_single_ledgers++
            LedgersFragment.adapter?.notifyDataSetChanged()

            DatabaseUtill().updateUserTotalLedgerCount(User.userID!!,object :OnUserDetailUpdate{
                override fun onUserDetailsUpdated(boolean: Boolean) {
                    if (boolean)
                        Log.d("UtillFunctions", "onUserDetailsUpdated: total number of ledgers updated ")

                }

            })
        }

        fun updateUserLedgers(user_single_Ledgers: ArrayList<SingleLedgers>){

            User.setUserSingleLedger(user_single_Ledgers)
            User.total_single_ledgers=user_single_Ledgers.size
            LedgersFragment.adapter?.notifyDataSetChanged()

            DatabaseUtill().updateUserTotalLedgerCount(User.userID!!,object :OnUserDetailUpdate{
                override fun onUserDetailsUpdated(boolean: Boolean) {
                    if (boolean)
                    Log.d("UtillFunctions", "onUserDetailsUpdated: total number of ledgers updated ")

                }

            })
        }




        fun showProgressDialog(dialog: AlertDialog){
            dialog.show();
        }

        fun hideProgressDialog(dialog: AlertDialog){
            dialog.dismiss();
        }
        fun setProgressDialog(context: Context, text:String):AlertDialog {
            val llPadding = 30
            val ll = LinearLayout(context)
            ll.orientation = LinearLayout.HORIZONTAL
            ll.setPadding(llPadding, llPadding, llPadding, llPadding)
            ll.gravity = Gravity.CENTER
            var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            ll.layoutParams = llParam
            val progressBar = ProgressBar(context)
            progressBar.isIndeterminate = true
            progressBar.setPadding(-30, 0, 0, 0)
            progressBar.layoutParams = llParam
            llParam = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            val tvText = TextView(context)
            tvText.text = text
            tvText.setTextColor(Color.parseColor("#000000"))
            tvText.textSize = 15f
            tvText.layoutParams = llParam
            ll.addView(progressBar)
            ll.addView(tvText)
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(ll)
            val dialog: AlertDialog = builder.create()
            val window: Window? = dialog.getWindow()
            if (window != null) {
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(dialog.getWindow()!!.getAttributes())
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                dialog.getWindow()!!.setAttributes(layoutParams)
            }
        return dialog
        }
    }
}