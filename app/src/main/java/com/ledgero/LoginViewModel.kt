package com.ledgero

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ledgero.model.DatabaseUtill
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {


     var auth: FirebaseAuth= Firebase.auth
private var TAG="LoginViewModel:"
    var userEmail=""
    var userPassword=""
 lateinit   var context: Context

 fun setmyContext( dcontext:Context){
     this.context=dcontext
 }

    fun isUserLogin():Boolean{

        val currentUser= auth.currentUser
        if (currentUser != null){
            Log.d(TAG, "isUserLogin: true ")
            return true
        }else {
            Log.d(TAG, "isUserLogin: false")
            return false}
    }

    fun validateUserInfo(): Boolean {

        if (!isValidEmail(userEmail)){
            Log.d(TAG, "validateUserInfo: Email Is Not Valid")
            Toast.makeText(context, "Email Is Not Valid", Toast.LENGTH_SHORT).show()
        return false
        }


        if (!isPasswordValid(userPassword)){
            Log.d(TAG, "ValidateUserInfo: Password Incorrect")
            Toast.makeText(
                context,
                "Password must be of 6 or more characters",
                Toast.LENGTH_LONG
            )
                .show()
        return false
        }

        return true
    }

    private fun isPasswordValid(pass:String): Boolean {

        if (pass.isNullOrBlank() || pass.length < 6) {
            return false
        }

        return true
    }

    private fun isValidEmail(email:String):Boolean{

        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun signIn() {
        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser

//                    //call this function to update current user data
//                    //so whenever user login its data will be fetched from
//                    //firebase and be updated
                    GlobalScope.launch {
                        var user = async { DatabaseUtill().updateCurrentUser(user!!.uid) }
                        Log.d(TAG, "signIn: ${user.await()}")
                    }

                   context.startActivity(Intent(context,MainActivity::class.java))
                    val ac= context as Activity
                    ac.finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(context, "Can't Find The User",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }
}