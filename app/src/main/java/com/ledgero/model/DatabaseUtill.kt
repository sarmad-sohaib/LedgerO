package com.ledgero.model

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.FetchUsers
import com.ledgero.Interfaces.OnUpdateUserSingleLedger
import com.ledgero.Interfaces.OnUserDetailUpdate


//all operations to do on server/db side
class DatabaseUtill {

    private val TAG ="DatabaseUtill"
    var db_reference= FirebaseDatabase.getInstance().reference

    companion object{

       var firebase_LedgersListener: ChildEventListener? =null

    }




    constructor() {

    }

    fun sendLedgerRequestToFriend(friendUID:String,ledger:SingleLedgers,callback: OnUserDetailUpdate){

      
      db_reference.child("users").child(friendUID).child("user_single_Ledgers")
          .get().addOnCompleteListener(){
              var arrayList:ArrayList<SingleLedgers>
              
              if (it.result.exists()){
                  arrayList= it.result.getValue<ArrayList<SingleLedgers>>()!!
                  arrayList.add(ledger)
                  db_reference.child("users").child(friendUID).child("user_single_Ledgers").setValue(arrayList)
                      .addOnCompleteListener(){
                          if (it.isSuccessful){
                              Log.d(TAG, "sendLedgerRequestToFriend: accepted")
                              callback.onUserDetailsUpdated(true)
                          }
                          if (it.isCanceled){
                              Log.d(TAG, "sendLedgerRequestToFriend: canceled -> ${it.exception}")
                              callback.onUserDetailsUpdated(false)
                          }
                      }
              }else{
                  Log.d(TAG, "sendLedgerRequestToFriend: Friend Request Failure -> ${it.exception}")
                  var list= ArrayList<SingleLedgers>()
                  list.add(ledger)
                  db_reference.child("users").child(friendUID).child("user_single_Ledgers").setValue(list)
                      .addOnCompleteListener(){
                          if (it.isSuccessful){
                              Log.d(TAG, "sendLedgerRequestToFriend: Friend Request Accepted ")

                              callback.onUserDetailsUpdated(true)

                          }else{

                              Log.d(TAG, "sendLedgerRequestToFriend: Friend Request failed completely! ")

                              callback.onUserDetailsUpdated(false)

                          }
                      }

              }
          }

    }

    fun getUser(email:String, userCallBack:FetchUsers){
        Log.d(TAG, "getUser: Called")

        db_reference.child("users").orderByChild("userEmail").equalTo(email)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        var userUID:String="null"
                        var userName:String="null"
                        var userEmail:String="null"
                        Log.d(TAG, "onDataChange: User with given email fetched")
                        snapshot.children.forEach(){
                           userUID=it.child("userID").value.toString()
                           userName=it.child("userName").value.toString()
                           userEmail=it.child("userEmail").value.toString()
                       }

                        Log.d(TAG, "onDataChange: User with given email fetched: $userUID")
                        userCallBack.OnSingleUserFetched(userUID,userEmail, userName)
                    }else{

                        Log.d(TAG, "onDataChange: No user found")
                        userCallBack.OnSingleUserFetched(null,null,null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: $error")
                  userCallBack.OnSingleUserFetched(null,null,null)
                }

            })





    }


//     fun getAllUsersData(usersFetched: FetchUsers){
//         Log.d(TAG, "getAllUsersData: called")
//        db_reference.child("users").orderByChild("userEmail").eqaddValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // Get Post object and use the values to update the UI
//              var usersList = ArrayList<FriendUsers>()
//                Log.d(TAG, "onDataChange: called")
//                for (ds in dataSnapshot.children) {
//                    var user = ds.getValue(FriendUsers::class.java)
//                    Log.d(TAG, "onDataChange: Read Users ${user.toString()} ")
//                    if (user!= null){
//                        if (!user?.userID.equals(Firebase.auth.currentUser?.uid) ){
//                            usersList?.add(user!!)
//                            Log.d(TAG, "onDataChange: ${user.userEmail} ")
//                        }}
//
//                }
//                usersFetched.OnAllUsersFetched(usersList)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d(TAG, "onCancelled: Can't fetch the users")
//            }
//        })
//
//    }

    fun updateCurrentUser(uid:String, callback:OnUserDetailUpdate):User{
        Log.d(TAG, "updateCurrentUser: Called")
        var user:User= User
        db_reference.child("users").child(user.userID!!).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    Log.d(TAG, "onDataChange: Current User Data Fetched")
                    user= snapshot.getValue<User>()!!

                    callback.onUserDetailsUpdated(true)

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
 fun createNewSingleLedger(uid: String,newLedger:SingleLedgers,newLedgerList: ArrayList<SingleLedgers>, callback: OnUpdateUserSingleLedger) {

        Log.d(TAG, "createNewSingleLedger: "+uid)
        //create new entry field in /ledgersEntries/
db_reference.child("users").child(uid).child("user_single_Ledgers").setValue(newLedgerList)
    .addOnCompleteListener(){
        if (it.isSuccessful){

            //creating ledger entry
            db_reference.child("ledgerEntries").child(newLedger.ledgerUID.toString()).child("isActive").setValue(true).addOnCompleteListener(){

                if (it.isSuccessful){
                    Log.d(TAG, "createNewSingleLedger: new Ledger Entry Created")
                    callback.onSingleLedgerUpdated(true)
                }
                if (it.isCanceled){
                    Log.d(TAG, "createNewSingleLedger: new Ledger Entry Creation Failed")
                    callback.onSingleLedgerUpdated(false)

                }
            }


        }
        if (it.isCanceled){
            Log.d(TAG, "createNewSingleLedger: Cannot update the ledger")
            callback.onSingleLedgerUpdated(false)

        }
    }


 }


    fun updateUserTotalLedgerCount(uid:String,callback:OnUserDetailUpdate){
        Log.d(TAG, "updateUserTotalLedgerCount: called")

        db_reference.child("users").child(uid).child("total_single_ledgers").setValue(User.getUserSingleLedgers()?.size)
            .addOnCompleteListener(){
                if (it.isSuccessful){

                    Log.d(TAG, "updateUserTotalLedgerCount: updated succesfully")
                    callback.onUserDetailsUpdated(true)
                }
                if (it.isCanceled){
                    Log.d(TAG, "updateUserTotalLedgerCount: not able to update user info")
                    callback.onUserDetailsUpdated(false)
                }

            }

        }

    //this will keep listening on user single ledgers in firebase...so if any change happen it will fire

   fun initListener(){
       if (firebase_LedgersListener!=null){
           return
       }
       firebase_LedgersListener= object :ChildEventListener{

           override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
               if (snapshot.exists()){
                   var newLedger = snapshot.getValue<SingleLedgers>()

                   var  ledgerAlreadyAdded=false
                   User.getUserSingleLedgers()?.forEach {
                       if (it!=null){
                           if (it.ledgerUID.equals(newLedger!!.ledgerUID))
                           {
                               ledgerAlreadyAdded=true
                           }
                       }
                   }
                   if (!ledgerAlreadyAdded){
                       UtillFunctions.addNewSingleUserLedgers(newLedger!!)
                   }
               }
           }

           override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
               Log.d(TAG, "onChildChanged: Ledgers Changed")

           }

           override fun onChildRemoved(snapshot: DataSnapshot) {

               if(snapshot.exists()) {
                   var newledger = snapshot.getValue<SingleLedgers>()
                   var ledgerUID = newledger!!.ledgerUID
                   UtillFunctions.removeSingleUserLedgers(newledger)
                   removeLedgerFromFriend(newledger)
                   removeLedgerFromFirebase(newledger)
               }
           }

           override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
               TODO("Not yet implemented")
           }

           override fun onCancelled(error: DatabaseError) {
               TODO("Not yet implemented")
           }

       }

   }

    private fun removeLedgerFromFirebase(newledger: SingleLedgers) {
        db_reference.child("ledgerEntries").child(newledger.ledgerUID.toString()).setValue(null).addOnCompleteListener(){
            if (it.isSuccessful){

                Log.d(TAG, "removeLedgerFromFirebase: Ledger Deleted")
            }
            if (it.isCanceled){
                Log.d(TAG, "removeLedgerFromFirebase: could not delete Ledgers")
            }
        }

    }

    private fun removeLedgerFromFriend(newledger: SingleLedgers) {

        db_reference.child("users").child(newledger.friend_userID!!).child("user_single_Ledgers").get()
            .addOnCompleteListener(){
                if (it.result.exists()){
                    var arrayList= it.result.getValue<ArrayList<SingleLedgers>>()!!
                 for (i in 0 until arrayList.size){
                     if(arrayList.get(i).friend_userID.equals(User.userID)){
                         arrayList.removeAt(i)
                     }
                 }

                    db_reference.child("users").child(newledger.friend_userID!!).child("user_single_Ledgers").setValue(arrayList)
                        .addOnCompleteListener(){
                            if (it.isSuccessful){
                                Log.d(TAG, "ledgerFromFriendDeleted: succesfully")

                            }
                            if (it.isCanceled){
                                Log.d(TAG, "ledgerFromFriendDeleted: failed")

                            }
                        }
                }
            }
    }

    fun UserLedgerListner(){

        initListener()
      var path=  db_reference.child("users").child(User.userID!!).child("user_single_Ledgers")
        path.addChildEventListener(firebase_LedgersListener!!)

    }

  fun  RemoveUserLedgerListner(){

      if(firebase_LedgersListener!=null){
          var path=  db_reference.child("users").child(User.userID!!).child("user_single_Ledgers")

          path.removeEventListener(firebase_LedgersListener!!)
      }
  }
}