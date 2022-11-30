package com.ledgero.groupLedger.flowStates

import com.ledgero.DataClasses.GroupLedgers

sealed class AllGroupsStateFlow {

    data class GroupUpdated(val group: GroupLedgers): AllGroupsStateFlow()
    data class AllGroupsFetched(val groups: ArrayList<GroupLedgers>): AllGroupsStateFlow()
    data class NewGroupAdded(val group: GroupLedgers): AllGroupsStateFlow()
    data class GroupRemoved(val group: GroupLedgers): AllGroupsStateFlow()

}