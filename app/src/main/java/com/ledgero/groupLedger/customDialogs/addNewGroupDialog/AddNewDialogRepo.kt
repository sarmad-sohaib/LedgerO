package com.ledgero.groupLedger.customDialogs.addNewGroupDialog

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.ledgero.DataClasses.GroupLedgers
import com.ledgero.DataClasses.User
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.UserSearchFlowStates
import com.ledgero.groupLedger.data.GroupInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "AddNewDialogRepo"
private const val NO_USER_FOUND_MESSAGE = "No Such User Found"
private const val USER_SEARCH_ERROR_MESSAGE = "Sorry! Could Not Search User, Please Try Again Later"
private const val GROUP_INFO_PATH = "groupsInfo"

class AddNewDialogRepo @Inject constructor() {


    private val _userSearchFlow = MutableStateFlow<UserSearchFlowStates>(UserSearchFlowStates.Idle)
    val userSearchFlow: StateFlow<UserSearchFlowStates> = _userSearchFlow
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO

    private val dbReference = FirebaseDatabase.getInstance().reference


    suspend fun searchUser(userEmail: String) {
        if (userEmail == User.userEmail){
            _userSearchFlow.value= UserSearchFlowStates.Error("You will be added as Admin in group. Search for other members")
            return
        }
        _userSearchFlow.value = UserSearchFlowStates.Loading
        withContext(dispatcherIO) {
            dbReference.child("users").orderByChild("userEmail").equalTo(userEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            val member = Member()

                            Log.d(TAG, "onDataChange: User with given email fetched")
                            snapshot.children.forEach {

                                member.apply {
                                    name = it.child("userName").value.toString()
                                    email = it.child("userEmail").value.toString()
                                    uid = it.child("userID").value.toString()

                                }
                            }

                            _userSearchFlow.value = UserSearchFlowStates.Success(member)

                        } else {

                            Log.d(TAG, "onDataChange: No user found")
                            _userSearchFlow.value =
                                UserSearchFlowStates.Error(NO_USER_FOUND_MESSAGE)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: $error")
                        _userSearchFlow.value =
                            UserSearchFlowStates.Error(USER_SEARCH_ERROR_MESSAGE)

                    }

                })

        }
    }

   suspend fun createGroupInfoInstance(members: ArrayList<Member>, groupName: String): GroupInfo{
        val groupKey = dbReference.child(GROUP_INFO_PATH).push().key
        return createGroupInfo(groupName,members, groupKey!!)
    }
    suspend fun uploadGroupMetaData(groupInfo: GroupInfo) {
        withContext(dispatcherIO){


            dbReference.child(GROUP_INFO_PATH).child(groupInfo.groupUID).setValue(groupInfo).apply {
                await()
                addOnCompleteListener{ Log.d(TAG, "createGroupMetaData: Group Info Uploaded") }
            }
        }
    }
    suspend fun addGroupInfoInMembers(members: ArrayList<Member>, groupInfo: GroupInfo){

        withContext(dispatcherIO){

             members.forEach{
               async { addGroupInfoInSingleMember(it.uid,groupInfo) }
             }
           val result= async {
                addGroupInfoInSingleMember(User.userID.toString(),groupInfo)

            }

            result.await()
        }

    }
    private suspend fun addGroupInfoInSingleMember(memberUID:String, groupInfo: GroupInfo){

        val timeStamp = groupInfo.groupCreateServerTimestamp
        val groupLedger= GroupLedgers(groupInfo.groupName,groupInfo.groupUID,timeStamp)
       val memberGroupInfo= dbReference.child("users").child(memberUID).child("user_group_Ledgers").get()
            .await()
        val groupsList= ArrayList<GroupLedgers>()
        if (memberGroupInfo.exists()){
            for(group in memberGroupInfo.children){
                groupsList.add(group.getValue(GroupLedgers::class.java)!!)
            }
        }
        groupsList.add(groupLedger)


      dbReference.child("users").child(memberUID).updateChildren(mapOf("user_group_Ledgers" to groupsList)).await()



    }


    private suspend fun createGroupInfo(
        groupName: String,
        members: ArrayList<Member>,
        groupUID: String
    ): GroupInfo {

        val timeStamp=  getNewTimeStamp()
        return GroupInfo(
            groupName,
            groupUID,
            User.userID.toString(), members.size + 1, members,
             0f, timeStamp
        )

    }


  private suspend fun getNewTimeStamp(): String{
      val timeStamp: String

      dbReference.child("tempNode").setValue(mapOf("timeStamp" to ServerValue.TIMESTAMP)).await()
     val time=   dbReference.child("tempNode").get().await()

      timeStamp= time.child("timeStamp").value.toString()
      return timeStamp
  }
}