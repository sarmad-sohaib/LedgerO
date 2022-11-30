package com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates

sealed class SelectedMembersFlowState {
    data class MemberAdded(val memberName:String): SelectedMembersFlowState()
    data class MemberRemoved(val memberName:String): SelectedMembersFlowState()
    object Done: SelectedMembersFlowState()
    object LimitReached: SelectedMembersFlowState()
}