package com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates

import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member

sealed class UserSearchFlowStates {
    data class Success(val groupMember: Member) :
        UserSearchFlowStates()
    data class Error(val errorMessage: String) : UserSearchFlowStates()
    object Loading : UserSearchFlowStates()
    object Idle : UserSearchFlowStates()
}