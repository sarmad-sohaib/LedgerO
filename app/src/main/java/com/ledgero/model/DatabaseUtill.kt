package com.ledgero.model

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.FetchUsers
import com.ledgero.Interfaces.OnUpdateUserSingleLedger
import com.ledgero.Interfaces.OnUserDetailUpdate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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

                    //now call the function so all ledgers get latest meta-data
                    if (user.getUserSingleLedgers()!=null){
                        if (user.getUserSingleLedgers()!!.size>0)
                        {
                          //  updateAllLedgerMetaData(User.getUserSingleLedgers()!!,0,callback)

                        }
                    }


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

    private fun updateAllLedgerMetaData(
        ledgers: ArrayList<SingleLedgers>,
        position: Int,
        callback: OnUserDetailUpdate
    ) {

var pos= position
        if (ledgers.get(pos)==null){
            callback.onUserDetailsUpdated(true)
        }else
        {
            var i = ledgers.get(pos)
            db_reference.child("ledgerInfo")
                .child(i.ledgerUID!!)
                .get().addOnCompleteListener(){
                    if (it.isSuccessful){
                   //     i.ledger_Created_timeStamp= it.result.child("ledger_Created_timeStamp").getValue<Long>()
                        i.ledgerCreatedByUID= it.result.child("ledgerCreatedByUID").value.toString()
                        i.total_entries=it.result.child("total_entries").getValue<Int>()
                        i.total_amount= it.result.child("total_amount").getValue<Float>()
                        i.give_take_flag= it.result.child("give_take_flag").getValue<Boolean>()
                    }
                    updateAllLedgerMetaData(ledgers,pos++,callback)

                }
        }
    }

    fun updateuserSingleLedgersList(uid:String, callback:OnUpdateUserSingleLedger ){

;
    }
    //when user add a new friend to start ledger, we will call this
    //function after initializing singleLedger object
    //then we need to create/add this new ledger to this user+ the friend user
 fun createNewSingleLedger(uid: String,newLedger:SingleLedgers,newLedgerList: ArrayList<SingleLedgers>,
                           callback: OnUpdateUserSingleLedger) {

        Log.d(TAG, "createNewSingleLedger: " + uid)


        //TODO: Need To clean this up, make DAOs for different task

        //for now to create new ledger we will
        // Step 1- Make a new node in ledgerInfo and update meta data along with creating timestamp
        // Step 2- Fetch Creation Timestamp and update the newleder object
        // Step 3- Add Ledger in user's user single ledger list
        // Step 4- Call the callback being passed


        // -- Step 1 -- New Node in LedgerInfo

        var map = getMetaDataMapForLedger(newLedger)
        db_reference.child("ledgerInfo").child(newLedger.ledgerUID.toString()).setValue(map)
            .addOnCompleteListener() {
                if (it.isSuccessful) {

                // ---Step 2 ---
                // fetch timestamp created by firebase and update ledger created time


                db_reference.child("ledgerInfo")
                    .child(newLedger.ledgerUID.toString())
                    .child("ledger_Created_timeStamp")
                    .get().addOnCompleteListener() {

                        if (it.isSuccessful) {

                        var date = it.result.getValue<Long>()!!
                        newLedger.ledger_Created_timeStamp = date


                        //--Step 3 --
                        //create new entry field in /ledgersEntries/
                        db_reference.child("users").child(uid).child("user_single_Ledgers")
                            .setValue(newLedgerList)
                            .addOnCompleteListener() {
                                if (it.isSuccessful) {

                                    //-- Step 4 -- Callback

                                    Log.d(TAG, "createNewSingleLedger: Created new ledger")
                                    callback.onSingleLedgerUpdated(true)

                                }
                                if (it.isCanceled) {
                                    Log.d(TAG,
                                        "createNewSingleLedger: Cannot update the ledger")
                                    callback.onSingleLedgerUpdated(false)

                                }
                            }

                    }else{

                            Log.d(TAG, "createNewSingleLedger: Could Not Fetch CreatedTimeStamp and Update Ledger")
                            callback.onSingleLedgerUpdated(false)
                    }
                    }


            }else{
                    Log.d(TAG, "createNewSingleLedger: Could Not Upload Meta Data Of Ledfer")
                    callback.onSingleLedgerUpdated(false)
            }
    }

            }




    private fun getMetaDataMapForLedger(newLedger: SingleLedgers): HashMap<String, Any> {
       var map= HashMap<String, Any>()
        map.put("total_amount",newLedger.total_amount!!)
        map.put("total_entries",newLedger.total_entries!!)
        map.put("give_take_flag",newLedger.give_take_flag!!)
        map.put("ledgerCreatedByUID",newLedger.ledgerCreatedByUID!!)
        map.put("ledger_Created_timeStamp", ServerValue.TIMESTAMP)


        return map
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


    fun addNewEntryInLedger(ledgerUID:String, entry:Entries){

    }
}