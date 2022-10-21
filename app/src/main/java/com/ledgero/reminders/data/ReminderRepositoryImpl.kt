package com.ledgero.reminders.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.ledgero.reminders.reminders.data.ReminderRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "ReminderRepositoryImpl"

class ReminderRepositoryImpl @Inject constructor() : ReminderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser?.uid
    private val collectionKey = user + "Reminders"

    override suspend fun insertReminder(collectionKey: String, reminder: Reminder) {
        val collectionReference = db.collection(collectionKey)

        collectionReference.document().set(reminder)
    }

    
    override suspend fun updateReminder(reminder: Reminder) {
        val collectionReference = db.collection(collectionKey)

        collectionReference
            .whereEqualTo("id", reminder.id)
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val docId = document.id
                    Log.i(TAG, "updateReminder: $docId")
                    collectionReference.document(docId).set(reminder)
                }
            }
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        val collectionReference = db.collection(collectionKey)

        collectionReference
            .whereEqualTo("id", reminder.id)
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot) {
                    collectionReference.document(document.id).delete()
                }
            }
    }

    override suspend fun getRemindersStream(collectionKey: String): Flow<List<Reminder?>> {
        return if (collectionKey != null) {
            db.collection(collectionKey)
                .whereEqualTo("complete", false)
                .snapshotFlow()
                .map { snapshot ->
                    snapshot.documents.map {
                        it.toObject(Reminder::class.java)
                    }
                }
        } else {
            emptyFlow()
        }
    }

    override suspend fun getCompletedRemindersStream(): Flow<List<Reminder?>> {
        return db.collection(collectionKey)
            .whereEqualTo("complete", true)
            .snapshotFlow()
            .map { snapshot ->
                snapshot.documents.map {
                    it.toObject(Reminder::class.java)
                }
            }
    }


    private fun Query.snapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
        val listenerRegistration = addSnapshotListener { value, error ->
            if (error != null) {
                close()
                return@addSnapshotListener
            }
            if (value != null)
                trySend(value)
        }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}