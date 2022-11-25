package com.ledgero.groupLedger.customDialogs.addNewGroupDialog

import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.UserSearchFlowStates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class AddNewDialogRepo @Inject constructor() {

    private val _userSearchFlow= MutableStateFlow<UserSearchFlowStates>(UserSearchFlowStates.Idle)
    val userSearchFlow: StateFlow<UserSearchFlowStates> = _userSearchFlow
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO

    fun searchUser(userEmail:String){





    }


}