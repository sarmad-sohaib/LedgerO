package com.ledgero.groupLedger.flowStates

import com.ledgero.DataClasses.GroupLedgersInfo

sealed class AllGroupsStateFlow {

    data class GroupUpdated(val group: GroupLedgersInfo): AllGroupsStateFlow()
    data class AllGroupsFetched(val groups: ArrayList<GroupLedgersInfo>): AllGroupsStateFlow()
    data class NewGroupAdded(val group: GroupLedgersInfo): AllGroupsStateFlow()
    data class GroupRemoved(val group: GroupLedgersInfo): AllGroupsStateFlow()

}