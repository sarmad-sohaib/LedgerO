package com.ledgero.model

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.ledgero.DataClasses.FriendUsers
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.FetchUsers
import com.ledgero.Interfaces.OnUpdateUserSingleLedger


class DatabaseUtill {

    private val TAG ="DatabaseUtill"
    var db_reference= FirebaseDatabase.getInstance().reference




    constructor() {

    }


    fun getUser(email:String, userCallBack:FetchUsers){
        Log.d(TAG, "getUser: Called")


        getAllUsersData(object : FetchUsers{
            override fun OnAllUsersFetched(users: ArrayList<FriendUsers>?) {
                var mUser: FriendUsers? =null


                if (users!=null){
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
               userCallBack.OnSingleUserFetched(mUser)
            }

            override fun OnSingleUserFetched(user: FriendUsers?) {
                ;//nothing to do here
            }
        })

    }


     fun getAllUsersData(usersFetched: FetchUsers){
         Log.d(TAG, "getAllUsersData: called")
        db_reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
              var usersList = ArrayList<FriendUsers>()
                Log.d(TAG, "onDataChange: called")
                for (ds in dataSnapshot.children) {
                    var user = ds.getValue(FriendUsers::class.java)
                    Log.d(TAG, "onDataChange: Read Users ${user.toString()} ")
                    if (user!= null){
                        if (!user?.userID.equals(Firebase.auth.currentUser?.uid) ){
                            usersList?.add(user!!)
                            Log.d(TAG, "onDataChange: ${user.userEmail} ")
                        }}

                }
                usersFetched.OnAllUsersFetched(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: Can't fetch the users")
            }
        })

    }

    fun updateCurrentUser(uid:String):User{
        Log.d(TAG, "updateCurrentUser: Called")
        var user:User= User
        db_reference.child("users").child(uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    Log.d(TAG, "onDataChange: Current User Data Fetched")
                    user= snapshot.getValue<User>()!!


                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.details}")
            }

        }

        )
        Log.d(TAG, "updateCurrentUser: User Returned")
        return user
    }

    fun updateuserSingleLedgersList(uid:String, callback:OnUpdateUserSingleLedger ){

;
    }
    //when user add a new friend to start ledger, we will call this
    //function after initializing singleLedger object
    //then we need to create/add this new ledger to this user+ the friend user
 fun createNewSingleLedger(uid: String,newLedgerList: ArrayList<SingleLedgers>, callback: OnUpdateUserSingleLedger) {

        //create new entry field in /ledgersEntries/
db_reference.child("users").child(uid).child("user_single_Ledgers").setValue(newLedgerList)
    .addOnCompleteListener(){
        if (it.isSuccessful){
            Log.d(TAG, "createNewSingleLedger: added successfully")
        }
    }


 }


}