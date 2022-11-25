package com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates

sealed class UserSearchFlowStates {
   data class Success(val userUID: String) : UserSearchFlowStates()
    data class Error(val message:String):UserSearchFlowStates()
    object Loading:UserSearchFlowStates()
    object Idle:UserSearchFlowStates()
}