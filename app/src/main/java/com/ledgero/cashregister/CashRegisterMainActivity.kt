package com.ledgero.cashregister

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ledgero.R
import dagger.hilt.android.AndroidEntryPoint

const val ADD_ENTRY_RESULT = Activity.RESULT_FIRST_USER
const val EDIT_ENTRY_RESULT = Activity.RESULT_FIRST_USER + 1

@AndroidEntryPoint
class CashRegisterMainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash_register_main)

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        val docRef = db.collection(currentUser.toString()).document()
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    Toast.makeText(this, ""+ (document.data?.get("name")), Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener{ exception ->
//                Log.e("Firestore error", exception.toString())
//            }
    }

}