package com.ledgero.utils

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ledgero.DataClasses.Entries
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.other.Constants.ADDED_IN_GROUP
import com.ledgero.other.Constants.REMOVED_FROM_GROUP
import com.ledgero.pushnotifications.PushNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

private const val TAG = "GroupEntriesHelp"

class GroupEntriesHelper {

    companion object {
        private var db_reference = FirebaseDatabase.getInstance().reference
        private var storage_reference = FirebaseStorage.getInstance().reference

        fun uploadAuidoFile(
            file: File,
            groupInfo: GroupInfo,
            entries: Entries,
            callBack: GroupVoiceUploadedInterface
        ) {
            var file = Uri.fromFile(file)
            var key = entries.entryUID!!
            var ref = storage_reference.child("voiceNotes").child(groupInfo.groupUID).child(key)
                .child("${file.lastPathSegment}")

            ref.putFile(file).addOnCompleteListener {

                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {

                            callBack.onVoiceUploaded(it.result.toString(), true)
                        } else {

                            callBack.onVoiceUploaded(it.toString(), false)

                        }
                    }

                } else {
                    callBack.onVoiceUploaded("null", false)

                }

            }


        }

        fun updateGroupEntry(
            groupInfo: GroupInfo,
            entries: Entries,
            callBack: GroupVoiceUploadedInterface
        ) {


            db_reference.child("groupEntries")
                .child(groupInfo!!.groupUID)
                .child(entries.entryUID.toString()).setValue(entries)
                .addOnCompleteListener {
                    if (it.isSuccessful) {

                        Log.d(
                            TAG,
                            " line 175: New Group Entry Added "
                        )

                        callBack.onGroupEntryAdded(true)
                    } else {


                        Log.d(
                            TAG,
                            " line 175: Cannot Add Group Entry "
                        )
                        callBack.onGroupEntryAdded(false)

                    }
                }
        }


        fun leaveGroup(groupInfo: GroupInfo, memberUID: String, callback: GroupMembersInterface) {
            db_reference.child("users").child(memberUID).child("user_group_Ledgers")
                .get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.exists()) {

                            var groups = ArrayList<GroupInfo>()
                            it.result.children.forEach {
                                val g = it.getValue(GroupInfo::class.java)!!
                                groups.add(g)
                            }

                            var index=0

                            var isFound=false
                            groups.forEach {
                                if ( !isFound && groups[index].groupUID == groupInfo.groupUID) {

                                    isFound=true

                                }
                                if (!isFound) index++
                            }
                            try {
                                groups.removeAt(index)

                            }catch (e:Exception){
                                Log.d(TAG, "leaveGroup: ${e.message}")
                            }


                            db_reference.child("users").child(memberUID).child("user_group_Ledgers")
                                .setValue(groups).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.d(
                                            TAG,
                                            "removeMemberFromGroup: group removed from member"
                                        )

                                        //if the member id us admin id it means group is going to be deleted
                                        if (memberUID == groupInfo.groupAdminUID) {
                                            callback.onGroupMembersUpdated(true)
                                        } else {

                                            removeMemberFromGroupInfo(
                                                memberUID,
                                                groupInfo,
                                                callback
                                            )


                                        }
                                    } else {
                                        callback.onGroupMembersUpdated(false)

                                        Log.d(
                                            TAG,
                                            "removeMemberFromGroup: could not upload updated groups in user node"
                                        )
                                    }
                                }


                        } else {
                            callback.onGroupMembersUpdated(false)

                            Log.d(
                                TAG,
                                "removeMemberFromGroup: no user's groups found --snapshot does not exist"
                            )
                        }

                    } else {
                        callback.onGroupMembersUpdated(false)
                        Log.d(TAG, "removeMemberFromGroup:  -- cannot fetch user's groups ")
                    }
                }
        }

        private fun removeMemberFromGroupInfo(
            memberUID: String,
            groupInfo: GroupInfo,
            callback: GroupMembersInterface
        ) {
            db_reference.child("groupsInfo").child(groupInfo.groupUID)
                .child("groupMembers")
                .get().addOnCompleteListener { it ->
                    if (it.isSuccessful) {


                        if (it.result.exists()) {

                            var members = ArrayList<Member>()
                            it.result

                            it.result.children.forEach { t ->
                                val m = t.getValue(Member::class.java)!!
                                members.add(m)
                            }

                            var index=0
                            var isFound =false

                            members.forEach{
                                if (!isFound && members[index].uid == memberUID) {

                                        isFound=true
                                }
                                if (!isFound) index++
                            }


                            val map = mapOf(
                                "groupTotalMembers" to members.size,
                                "groupMembers" to members
                            )
                            db_reference.child("groupsInfo")
                                .child(groupInfo.groupUID)
                                .updateChildren(map)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        PushNotification().createAndSendNotificationSingleGroupMember(memberUID,REMOVED_FROM_GROUP)
                                        callback.onGroupMembersUpdated(true)

                                        Log.d(
                                            TAG,
                                            "removeMemberFromGroup: group info updated"
                                            //function completes here
                                        )
                                    } else {
                                        callback.onGroupMembersUpdated(false)

                                        Log.d(
                                            TAG,
                                            "removeMemberFromGroup: cannot update group info"
                                        )
                                    }
                                }


                        } else {
                            callback.onGroupMembersUpdated(false)

                            Log.d(
                                TAG,
                                "removeMemberFromGroup: no group info found...data snapshot is empty"
                            )
                        }

                    } else {
                        callback.onGroupMembersUpdated(false)

                        Log.d(
                            TAG,
                            "removeMemberFromGroup: cannot update group info"
                        )
                    }
                }
        }

        private fun addMemberInGroupInfo(
            member: Member,
            groupInfo: GroupInfo,
            callback: GroupMembersInterface
        ) {
            db_reference.child("groupsInfo").child(groupInfo.groupUID)
                .child("groupMembers")
                .get().addOnCompleteListener { it ->
                    if (it.isSuccessful) {

                        var members = ArrayList<Member>()

                        if (it.result.exists()) {

                            it.result.children.forEach { t ->
                                val m = t.getValue(Member::class.java)!!
                                members.add(m)
                            }
                        }
                            members.add(member)

                            val map = mapOf(
                                "groupTotalMembers" to members.size,
                                "groupMembers" to members
                            )
                            db_reference.child("groupsInfo")
                                .child(groupInfo.groupUID)
                                .updateChildren(map)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        callback.onGroupMembersUpdated(true)

                                        Log.d(
                                            TAG,
                                            "removeMemberFromGroup: group info updated"
                                            //function completes here
                                        )
                                    } else {
                                        callback.onGroupMembersUpdated(false)

                                        Log.d(
                                            TAG,
                                            "removeMemberFromGroup: cannot update group info"
                                        )
                                    }
                                }




                    } else {
                        callback.onGroupMembersUpdated(false)

                        Log.d(
                            TAG,
                            "removeMemberFromGroup: cannot update group info"
                        )
                    }
                }
        }

        fun deleteGroupForAll(
            groupInfo: GroupInfo,
            adminUID: String,
            callback: GroupMembersInterface
        ) {
            db_reference.child("groupsInfo").child(groupInfo.groupUID)
                .child("groupMembers")
                .get().addOnCompleteListener { it ->
                    if (it.isSuccessful) {


                        if (it.result.exists()) {

                            var members = ArrayList<Member>()


                            var flag = false
                            it.result.children.forEach { t ->
                                val m = t.getValue(Member::class.java)!!
                                members.add(m)
                            }
                            for (i in 0 until members.size) {
                                leaveGroup(
                                    groupInfo,
                                    members[i].uid,
                                    object : GroupMembersInterface {
                                        override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                                            flag = isSuccessFull
                                        }

                                    })
                            }

                            leaveGroup(
                                groupInfo,
                                groupInfo.groupAdminUID,
                                object : GroupMembersInterface {
                                    override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                                        if (isSuccessFull) {
                                            callback.onGroupMembersUpdated(true)
                                        } else {
                                            callback.onGroupMembersUpdated(false)

                                        }
                                    }

                                })


                        } else {
                            leaveGroup(
                                groupInfo,
                                groupInfo.groupAdminUID,
                                object : GroupMembersInterface {
                                    override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                                        if (isSuccessFull) {
                                            callback.onGroupMembersUpdated(true)
                                        } else {
                                            callback.onGroupMembersUpdated(false)

                                        }
                                    }

                                })



                            Log.d(
                                TAG,
                                "removeMemberFromGroup: no group info found...data snapshot is empty"
                            )
                        }

                    } else {
                        callback.onGroupMembersUpdated(false)


                        Log.d(
                            TAG,
                            "removeMemberFromGroup: cannot update group info"
                        )
                    }
                }
        }


        fun searchUser(userEmail: String, callback: AddNewMember) {

            db_reference.child("users").orderByChild("userEmail").equalTo(userEmail)
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
                            callback.onUserSearch(true, member)


                        } else {
                            callback.onUserSearch(false, null)


                        }
                    }

                    override fun onCancelled(error: DatabaseError) =
                        callback.onUserSearch(false, null)


                })


        }


        private suspend fun addGroupInfoInSingleMember(
            member: Member,
            groupInfo: GroupInfo,
            callback: AddNewMember
        ) {


            val memberGroupInfo =
                db_reference.child("users").child(member.uid).child("user_group_Ledgers").get()
                    .await()
            val groupsList = ArrayList<GroupInfo>()
            if (memberGroupInfo.exists()) {
                for (group in memberGroupInfo.children) {
                    groupsList.add(group.getValue(GroupInfo::class.java)!!)
                }
            }
            groupsList.add(groupInfo)


            db_reference.child("users").child(member.uid)
                .updateChildren(mapOf("user_group_Ledgers" to groupsList)).await()

            callback.onUserSearch(true, member)


        }

        fun addNewMemberInExistedGroup(
            groupInfo: GroupInfo,
            member: Member,
            callback: GroupMembersInterface
        ) {

            CoroutineScope(Dispatchers.Main).launch {
                addGroupInfoInSingleMember(member, groupInfo, object : AddNewMember {
                    override fun onUserSearch(isSuccessFull: Boolean, theMember: Member?) {

                        addMemberInGroupInfo(member, groupInfo, object : GroupMembersInterface {
                            override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                                if (isSuccessFull){
                                    PushNotification().createAndSendNotificationSingleGroupMember(member.uid,
                                        ADDED_IN_GROUP)
                                    callback.onGroupMembersUpdated(true)

                                } else callback.onGroupMembersUpdated(false)
                            }

                        })
                    }

                })
            }

        }

         fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun sendNotificationToAllMember(groupInfo: GroupInfo, type: Int) {
            db_reference.child("groupsInfo").child(groupInfo.groupUID).child("groupMembers").get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val member = ArrayList<Member>()
                        it.children.forEach{
                            val m= it.getValue(Member::class.java)!!
                            member.add(m)
                        }

                        member.forEach {
                            PushNotification().createAndSendNotificationSingleGroupMember(it.uid,type)
                        }
                    }
                }

        }

    }


}

interface GroupVoiceUploadedInterface {

    fun onVoiceUploaded(downloadURI: String, isSuccessFull: Boolean)
    fun onGroupEntryAdded(isSuccessFull: Boolean)
}

interface GroupMembersInterface {
    fun onGroupMembersUpdated(isSuccessFull: Boolean)
}

interface AddNewMember {

    fun onUserSearch(isSuccessFull: Boolean, theMember: Member?)
}

interface AllGroupsAmount{
    fun onAllGroupsAmount()
}