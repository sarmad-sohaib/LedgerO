package com.ledgero.firebasetokens

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseTokenManager {
    companion object {
        private var dbReference = FirebaseDatabase.getInstance().reference

        suspend fun getNotificationTokenOfRecipient(recipientUserID: String): String? {


            var data = dbReference
                .child("tokens")
                .child(recipientUserID)
                .child("firebaseToken")
                .get()
                .await()
            return data.value.toString()

        }

        fun registerUserFirebaseToken(userID: String) {

            CoroutineScope(Dispatchers.IO).launch {
                val result = async { FirebaseMessaging.getInstance().token }
                val token = result.await().result
                if (token != null) {
                    dbReference.child("tokens")
                        .child(userID)
                        .child("firebaseToken")
                        .setValue(token)
                }
            }
        }

        fun updateUserFirebaseToken(userID: String, token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                if (token != null) {
                    dbReference.child("tokens")
                        .child(userID)
                        .child("firebaseToken")
                        .setValue(token)
                }
            }
        }

    }
}
