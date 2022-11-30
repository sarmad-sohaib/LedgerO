package com.ledgero.groupLedger

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.DataClasses.GroupLedgers
import com.ledgero.DataClasses.User
import com.ledgero.groupLedger.flowStates.AllGroupsStateFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "GroupLedgerRepo"

class GroupLedgerFragmentRepo @Inject constructor() {

    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
    private val dispatcherDefault: CoroutineDispatcher = Dispatchers.Default
    private val dbReference = FirebaseDatabase.getInstance().reference
    private val _userAllGroupsFlow = MutableSharedFlow<AllGroupsStateFlow>()
    val userAllGroupsStateFlow: SharedFlow<AllGroupsStateFlow> = _userAllGroupsFlow


    suspend fun getAllGroups() {
        Log.d(TAG, "getAllGroups: called")
        withContext(dispatcherIO) {

            val allGroups =
                dbReference.child("users").child(User.userID.toString()).child("user_group_Ledgers")
                    .get().await()

            if (allGroups.exists()) {
                val groupsList = ArrayList<GroupLedgers>()
                Log.d(TAG, "getAllGroups: ${allGroups.value.toString()}")
                allGroups.children.forEach {
                    val g= it.getValue(GroupLedgers::class.java)!!
                    Log.d(TAG, "getAllGroups: ${g.groupName}")
                    groupsList.add(g)
                }
                Log.d(TAG, "getAllGroups: emitting all groups")
                _userAllGroupsFlow.emit(AllGroupsStateFlow.AllGroupsFetched(groupsList))
                }
                startListeningForGroupChanges()
            }
        }


    private suspend fun startListeningForGroupChanges() {
        withContext(dispatcherIO) {
            dbReference.child("users").child(User.userID.toString()).child("user_group_Ledgers")
                .addChildEventListener(allGroupsObserver)
        }


    }

  private val allGroupsObserver= object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val group = snapshot.getValue(GroupLedgers::class.java)!!

                CoroutineScope(dispatcherDefault).launch {
                    _userAllGroupsFlow.emit(AllGroupsStateFlow.NewGroupAdded(group))
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val group = snapshot.getValue(GroupLedgers::class.java)!!

                CoroutineScope(dispatcherDefault).launch {
                    _userAllGroupsFlow.emit(AllGroupsStateFlow.GroupUpdated(group))
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val group = snapshot.getValue(GroupLedgers::class.java)!!
                CoroutineScope(dispatcherDefault).launch {
                    _userAllGroupsFlow.emit(AllGroupsStateFlow.GroupRemoved(group))
                }


            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) = Unit

        }


    fun stopObservers() {
        dbReference.child("users").child(User.userID.toString()).child("user_group_Ledgers")
            .removeEventListener(allGroupsObserver)
    }


}