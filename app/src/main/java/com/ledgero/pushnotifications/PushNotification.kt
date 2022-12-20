package com.ledgero.pushnotifications

import android.annotation.SuppressLint
import android.util.Log
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.firebasetokens.FirebaseTokenManager
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.other.Constants.ADDED_IN_GROUP
import com.ledgero.pushnotifications.api.PushNotificationAPI
import com.ledgero.pushnotifications.data.NotificationData
import com.ledgero.pushnotifications.data.PushNotificationData
import com.ledgero.pushnotifications.di.RetrofitPushNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ledgero.other.Constants.ADD_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.DELETE_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.EDIT_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.ENTRY_ADDED_GROUP
import com.ledgero.other.Constants.ENTRY_APPROVED
import com.ledgero.other.Constants.ENTRY_REJECTED
import com.ledgero.other.Constants.ENTRY_REMOVED_GROUP
import com.ledgero.other.Constants.ENTRY_RESENT
import com.ledgero.other.Constants.REMOVED_FROM_GROUP


private const val TAG = "PushNotification"


class PushNotification {
    private var pushNotificationAPI: PushNotificationAPI = RetrofitPushNotification().getAPi()


    private fun sendPushNotification(notification: PushNotificationData) {

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val response = pushNotificationAPI.postNotification(notification)
                if (response.isSuccessful) {
//                    Log.d(TAG, "sendPushNotification: Response = ${Gson().toJson(response)}")
                } else {
                    Log.d(TAG, "sendPushNotification: ${response.errorBody().toString()}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "sendPushNotification: ${e.message}")
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun createAndSendNotification(ledgerUID: String, type:Int) {
        CoroutineScope(Dispatchers.Default).launch {

            var ledger = SingleLedgers()

            for (i in User.getUserSingleLedgers()!!) {

                if (i.ledgerUID == ledgerUID) {
                    ledger = i
                }
            }
            val recipientUID = if (ledger.ledgerCreatedByUID == User.userID) ledger.friend_userID.toString() else ledger.ledgerCreatedByUID.toString()
            val recipientToken =
                FirebaseTokenManager.getNotificationTokenOfRecipient(recipientUID)

            val notificationMessage = getNotificationMessage(type)

            val notification = NotificationData(notificationMessage.first,
                " ${notificationMessage.second}",
                ledgerUID,
                "nothing for now")
            if (recipientToken != null) {
                val pushNotification = PushNotificationData(notification, recipientToken)
                sendPushNotification(pushNotification)
            } else {
                Log.d(TAG, "createAndSendNotification: Recipient Token Not Found")
            }

        }
    }
    fun createAndSendNotificationSingleGroupMember(memberUID: String, type:Int) {
        CoroutineScope(Dispatchers.Default).launch {


            val recipientToken =
                FirebaseTokenManager.getNotificationTokenOfRecipient(memberUID)

            val notificationMessage = getNotificationMessage(type)

            val notification = NotificationData(notificationMessage.first,
                " ${notificationMessage.second}",
                memberUID,
                "nothing for now")
            if (recipientToken != null) {
                val pushNotification = PushNotificationData(notification, recipientToken)
                sendPushNotification(pushNotification)
            } else {
                Log.d(TAG, "createAndSendNotification: Recipient Token Not Found")
            }

        }
    }

    private fun getNotificationMessage(type:Int):Pair<String,String>{

          if (type == ADD_REQUEST_REQUEST_MODE) return Pair("Added New Entry","${User.userName} wants to add new entry \n Waiting for your approval")
          if (type == DELETE_REQUEST_REQUEST_MODE) return Pair("Delete Entry","${User.userName} wants to delete an entry \n Waiting for your approval.")
          if (type == EDIT_REQUEST_REQUEST_MODE) return Pair("Update Entry","${User.userName} wants to update an entry \n Waiting your approval.")
          if (type == ENTRY_APPROVED) return Pair("Entry Approved","${User.userName} approved your entry request.\n Check you ledger now")
          if (type == ENTRY_REJECTED) return Pair("Entry Rejected","${User.userName} rejected your entry request.\n Looks like you guys having a conflict")
          if (type == ENTRY_RESENT) return Pair("Entry Resent.","${User.userName} resent the entry.\n This needs your attention")
          if (type == ADDED_IN_GROUP) return Pair("Group Created.","${User.userName} added you in a group.")
          if (type == REMOVED_FROM_GROUP) return Pair("Group Removed.","You are no longer part of group")
          if (type == ENTRY_ADDED_GROUP) return Pair("Entry Added in Group.","${User.userName} added new entry in group")
          if (type == ENTRY_REMOVED_GROUP) return Pair("Entry Removed From Group.","${User.userName} removed an entry")

        return Pair("Action Performed","${User.userName} resent the entry.\n This needs your attention")




    }
}


