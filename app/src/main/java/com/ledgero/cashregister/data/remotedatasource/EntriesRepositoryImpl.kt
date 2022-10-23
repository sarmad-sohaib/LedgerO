package com.ledgero.cashregister.data.remotedatasource // notice Android studio warnings, this package was not right

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ledgero.cashregister.data.Entry
import com.ledgero.cashregister.data.EntryRepository
import javax.inject.Inject

private const val TAG = "EntriesRepoImpl"

// Not a good name
interface MyCallback {
    fun onCallback(value: List<Entry>)
}

class EntriesRepositoryImpl @Inject constructor() : EntryRepository {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var collectionReference: CollectionReference
    private lateinit var entry: Entry

    override suspend fun getEntries(uId: String, myCallback: MyCallback) {

        collectionReference = db.collection(uId)

        val list = mutableListOf<Entry>()

        collectionReference.addSnapshotListener { value, e ->
            // Kotlin has better nullability checks
            e?.let { Log.e("LIST", "Listen failed.", e) }

            if (value != null) {
                list.clear()
                for (doc in value) {
                    entry = doc.toObject(Entry::class.java)
                    list.add(entry)
                }
                myCallback.onCallback(list)
            }
        }
    }

    override suspend fun insertEntry(uId: String, entry: Entry) {
        collectionReference = db.collection(uId)
        collectionReference.document().set(entry)
    }

    override suspend fun deleteEntry(uId: String, id: String) {
        collectionReference = db.collection(uId)
        collectionReference
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener {
                // can be shortened with forEach, example below
                for (document in it) {
                    val docId = document.id
                    collectionReference.document(docId).delete()
                }
            }.addOnFailureListener {
                Log.e(TAG, "deleteEntry: $it")
            }
    }

    override suspend fun updateEntry(uId: String, id: String, entry: Entry) {
        collectionReference = db.collection(uId)
        collectionReference
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val docId = document.id
                    collectionReference.document(docId).set(entry)
                }
            }
    }

    override suspend fun getSumByProperty(uId: String, property: String) {
        collectionReference = db.collection(uId)

        collectionReference
            .whereEqualTo("out", property.toBoolean())
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Don't use magic strings and magic numbers
                querySnapshot.forEach { it.getString(QUERY_FIELD_AMOUNT) }
            }
    }

    companion object {
        private const val QUERY_FIELD_AMOUNT = "amount"
    }
}

// Looks good, except for Some formatting issues.
// To format a whole file at one: cmd + a then cmd + option + l