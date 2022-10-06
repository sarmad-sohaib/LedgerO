package com.ledgero.cashregister

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ledgero.cashregister.data.Entry
import com.ledgero.cashregister.data.EntryRepository
import javax.inject.Inject

private const val TAG = "EntriesRepoImpl"

interface MyCallback {
    fun onCallback(value: List<Entry>)
}

class EntriesRepositoryImpl @Inject constructor(): EntryRepository {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var collectionReference: CollectionReference
    private lateinit var entry: Entry

    override suspend fun getEntries(uId: String, myCallback: MyCallback) {

        collectionReference = db.collection(uId)

        val list = mutableListOf<Entry>()

        collectionReference.addSnapshotListener { value, e ->
            if (e != null) {
                Log.e("LIST", "Listen failed.", e)
            }

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
            .addOnSuccessListener {
                for (document in it) {
                    document.getString("amount")
                }
            }
    }
}