package com.ledgero.groupLedger.customDialogs.addNewGroupDialog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.CreateGroupButtonStates
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.CreatingGroupStates
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.SelectedMembersFlowState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddNewGroupDialogVM"
private const val MAX_MEMBER_LIMIT = 4
private const val MIN_MEMBER_LIMIT = 2

@HiltViewModel
class DialogViewModel @Inject constructor(private val repo: AddNewDialogRepo) : ViewModel() {

    val userSearchFlow = repo.userSearchFlow
    var currentSearchedUser: Member? = null
    private val _selectedMembersFlow =
        MutableStateFlow<SelectedMembersFlowState>(SelectedMembersFlowState.Done)
    val selectedMembersFlow: StateFlow<SelectedMembersFlowState> = _selectedMembersFlow

    private val _creatingNewGroupFlow =
        MutableStateFlow<CreatingGroupStates>(CreatingGroupStates.Idle)
    val creatingNewGroupFlow: StateFlow<CreatingGroupStates> = _creatingNewGroupFlow


    private val _createGroupBtnFlow =
        MutableStateFlow<CreateGroupButtonStates>(CreateGroupButtonStates.Disabled)
    val createGroupBtnFlow: StateFlow<CreateGroupButtonStates> = _createGroupBtnFlow

    private var _totalMembersSelected = 0
    val totalMembersSelected get() = _totalMembersSelected
    var selectedGroupMembers = ArrayList<Member>()


    fun createGroup(groupName: String) {
        _creatingNewGroupFlow.value= CreatingGroupStates.Loading
        viewModelScope.launch {
            val groupInfo = repo.createGroupInfoInstance(selectedGroupMembers,groupName)
            val groupMetaData = async { repo.uploadGroupMetaData(groupInfo) }
            val groupAddInUsers =
                async { repo.addGroupInfoInMembers(selectedGroupMembers, groupInfo) }
            groupMetaData.await()
            groupAddInUsers.await()

            _creatingNewGroupFlow.value= CreatingGroupStates.Success
        }
    }

    fun searchUser(email: String) {

        viewModelScope.launch {
            repo.searchUser(email)
        }
    }

    fun isMemberAlreadySelected(userUID: String): Boolean {

        var flag = false

        selectedGroupMembers.forEach {
            if (it.uid == userUID) flag = true
        }

        Log.d(TAG, "isMemberAlreadySelected: $flag")
        return flag

    }

    fun addCurrentSelectedUserInGroupMember() {
        if (currentSearchedUser == null) {
            Log.d(TAG, "addCurrentSelectedUserInGroupMember: No User Selected")
            return
        }
        if (_totalMembersSelected < MAX_MEMBER_LIMIT) {
            Log.d(TAG, "addCurrentSelectedUserInGroupMember: Adding Member")
            selectedGroupMembers.add(currentSearchedUser!!)
            _totalMembersSelected++
            _selectedMembersFlow.value =
                SelectedMembersFlowState.MemberAdded(currentSearchedUser!!.name)
            Log.d(TAG, "addCurrentSelectedUserInGroupMember: Member added")
            if (totalMembersSelected >= MIN_MEMBER_LIMIT) _createGroupBtnFlow.value =
                CreateGroupButtonStates.Enabled

        } else {
            Log.d(TAG, "addCurrentSelectedUserInGroupMember: Max Member Limit Reached")

            _selectedMembersFlow.value = SelectedMembersFlowState.LimitReached

        }

    }

    fun newMemberAddedSuccessfully() {
        _selectedMembersFlow.value = SelectedMembersFlowState.Done
        currentSearchedUser = null
    }

    fun selectedMemberRemove(i: Int) {

        Log.d(TAG, "selectedMemberRemove: ${selectedGroupMembers[i - 1].name}")

        val member = selectedGroupMembers.removeAt(i - 1)
        _totalMembersSelected--
        _selectedMembersFlow.value = SelectedMembersFlowState.MemberRemoved(member.name)

        if (totalMembersSelected < MIN_MEMBER_LIMIT) _createGroupBtnFlow.value =
            CreateGroupButtonStates.Disabled
    }

}