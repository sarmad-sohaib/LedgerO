package com.ledgero.groupLedger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.DataClasses.GroupLedgers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG= "GroupLedgerViewModel"
@HiltViewModel
class GroupLedgerFragmentViewModel @Inject constructor(private val repo: GroupLedgerFragmentRepo) :  ViewModel() {

    val allGroupsListenerFlow = repo.userAllGroupsStateFlow
    var allGroups:ArrayList<GroupLedgers> = ArrayList<GroupLedgers>()


    init {
    }

   fun getAllGroups() {
      allGroups.add( GroupLedgers("demo","asda","123345432"))
       viewModelScope.launch{
           repo.getAllGroups()
       }

   }

    fun stopAllObservers() {
        repo.stopObservers()
    }

    fun isGroupExist(group: GroupLedgers):Boolean {

        var flag=false
        allGroups.forEach{
            if (it.groupUID == group.groupUID) flag=true
        }

        return flag

    }


}