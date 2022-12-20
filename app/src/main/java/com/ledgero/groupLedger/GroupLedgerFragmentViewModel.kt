package com.ledgero.groupLedger

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.DataClasses.GroupLedgersInfo
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.groupLedger.singleGroup.GroupEntry
import com.ledgero.other.Constants.CASH_IN
import com.ledgero.other.Constants.CASH_OUT
import com.ledgero.utils.AllGroupsAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.sql.Time
import java.time.Instant
import javax.inject.Inject


private const val TAG = "GroupLedgerViewModel"

@HiltViewModel
class GroupLedgerFragmentViewModel @Inject constructor(private val repo: GroupLedgerFragmentRepo) :
    ViewModel() {

    val allGroupsListenerFlow = repo.userAllGroupsStateFlow
    var allGroups: ArrayList<GroupInfo> = ArrayList<GroupInfo>()
    private val db = FirebaseDatabase.getInstance().reference

    var allEntries = ArrayList<GroupEntry>()

    var totalIn = 0f
    var totalOut = 0f

    init {
    }

    fun getAllGroups() {
        viewModelScope.launch {
            repo.getAllGroups()
        }

    }

    fun stopAllObservers() {
        repo.stopObservers()
    }

    fun isGroupExist(group: GroupLedgersInfo): Boolean {

        var flag = false
        allGroups.forEach {
            if (it.groupUID == group.groupUID) flag = true
        }

        return flag

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showAllGroupsAmount(callback: AllGroupsAmount) {

        Log.d(TAG, "showAllGroupsAmount: called")
        allEntries.clear()
        viewModelScope.launch {
            withContext(Dispatchers.IO){
            var index = 0
            async {

                val t = ArrayList(allGroups)
                        t.forEach {
                    getAmountOfGroup(it.groupUID, index)
                    index++
                }
            }.await()
                Log.d(TAG, "showAllGroupsAmount: each group amount calculated")

                delay(2000)
            calculateLast30DaysAmount()

            withContext(Dispatchers.Main){
                callback.onAllGroupsAmount()
            }
            }

        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateLast30DaysAmount() {
        Log.d(TAG, "calculateLast30DaysAmount: called")

        totalIn=0f
        totalOut=0f
        val currentTime =Time.from(
            Instant.now().minusSeconds(30 * 24 * 60 * 60)
        ).time.toInt()
        if (currentTime<0) currentTime * (-1)
        var entriesCopy= ArrayList(allEntries)
            entriesCopy.forEach { entry ->
                 if (entry.entry_timeStamp!!.toInt()> currentTime){
                    Log.d(TAG, "calculateLast30DaysAmount: ssIn =  $totalIn and out = $totalOut" )

                    if (entry.cashInOutFlag== CASH_IN) totalIn+=entry.amount!!
                    if (entry.cashInOutFlag== CASH_OUT) totalOut+= entry.amount!!
                }

            }


        Log.d(TAG, "calculateLast30DaysAmount: In =  $totalIn and out = $totalOut" )
    }

    private suspend fun getAmountOfGroup(groupUID: String, index: Int) {
        Log.d(TAG, "getAmountOfGroup: called")
            db.child("groupEntries").child(groupUID).get().addOnSuccessListener {

                if (it.exists()) {
                    val entries = ArrayList<GroupEntry>()
                    var totalCashIn = 0f
                    var totalCashOut = 0f
                    it.children.forEach {
                        val e = it.getValue(GroupEntry::class.java)!!
                        entries.add(e)
                        if (e.cashInOutFlag == CASH_IN) totalCashIn += e.amount!! else totalCashOut += e.amount!!

                    }
                    allEntries.addAll(entries)


                    var totalAmount = 0f

                    if (totalCashIn > totalCashOut) {
                        totalAmount = totalCashIn - totalCashOut
                        allGroups[index].groupTotalAmount = totalAmount
                        allGroups[index].cashInOut = CASH_IN
                    }
                    if (totalCashIn < totalCashOut) {
                        totalAmount = totalCashOut - totalCashIn

                        allGroups[index].groupTotalAmount = totalAmount
                        allGroups[index].cashInOut = CASH_OUT
                    }
                    if (totalCashIn == totalCashOut) {
                        allGroups[index].groupTotalAmount = totalAmount
                        allGroups[index].cashInOut = CASH_IN

                    }
                }
            }.await()

        


    }
}


