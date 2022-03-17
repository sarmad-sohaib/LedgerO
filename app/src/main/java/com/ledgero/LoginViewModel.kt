package com.ledgero

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginViewModel : ViewModel() {


     lateinit  var userPhone: String
   lateinit  var userPass: String

     var auth: FirebaseAuth= Firebase.auth
    val database: DatabaseReference = Firebase.database.reference


    fun checkLoginUser(): Boolean {
        var user =auth.currentUser

        if (user != null) return true
        else return false
    }

    fun checkUserInfo():Boolean{


        return false
    }

}