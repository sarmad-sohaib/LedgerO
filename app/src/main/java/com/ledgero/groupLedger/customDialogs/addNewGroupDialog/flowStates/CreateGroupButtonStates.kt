package com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates

sealed class CreateGroupButtonStates {

    object Enabled: CreateGroupButtonStates()
    object Disabled: CreateGroupButtonStates()

}