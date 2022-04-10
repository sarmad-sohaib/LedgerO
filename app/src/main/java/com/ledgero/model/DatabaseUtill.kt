package com.ledgero.model

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.ledgero.DataClasses.User

class DatabaseUtill {

    private val TAG ="DatabaseUtill"
    var db= FirebaseDatabase.getInstance().reference


   private var users : ArrayList<User> = ArrayList()

    constructor(){
        getAllUsersData()
    }


    fun getUser(email:String):User?{
        Log.d(TAG, "getUser: Called")
        var mUser: User? =null


        if (!users.isEmpty()){
            for (u in users){
                if (u.userEmail.equals(email)){
                    Log.d(TAG, "getUser: $email : found!!")
                    mUser=u
                }
            }
            if (mUser==null){
                Log.d(TAG, "getUser: $email : Not Found.")
            }
        }else{
            Log.d(TAG, "getUser: No Data To Find User ")
        }
        return mUser
    }


     fun getAllUsersData():ArrayList<User>{
         Log.d(TAG, "getAllUsersData: called")
        db.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.d(TAG, "onDataChange: called")
                for (ds in dataSnapshot.children) {
                    var user = ds.getValue(User::class.java)
                    Log.d(TAG, "onDataChange: Read Users ${user.toString()} ")
                    if (user!= null){
                        if (!user?.userID.equals(Firebase.auth.currentUser?.uid) ){
                            users.add(user!!)
                            Log.d(TAG, "onDataChange: ${user.userEmail} ")
                        }}

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: Can't fetch the users")
            }
        })

        return users
    }



}