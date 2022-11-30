package com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates

sealed class CreatingGroupStates {
    object Success: CreatingGroupStates()
    object Loading: CreatingGroupStates()
    object Idle: CreatingGroupStates()
    data class Error(val errorMessage:String): CreatingGroupStates()

}