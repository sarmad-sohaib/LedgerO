package com.ledgero.firebasetokens

import android.util.Log
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

        suspend fun getNotificationTokenOfRecipient(recipientUserID: String): String {


            val data = dbReference
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
try {

    val response= result.await()
    val token = response.result
    if (token != null) {
        dbReference.child("tokens")
            .child(userID)
            .child("firebaseToken")
            .setValue(token)
    }
}catch (e:Exception){
    Log.d("FirebaseTokenMan", "registerUserFirebaseToken: ${e.localizedMessage} ")
}

            }
        }

        fun updateUserFirebaseToken(userID: String, token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                dbReference.child("tokens")
                    .child(userID)
                    .child("firebaseToken")
                    .setValue(token)
            }
        }

    }
}
