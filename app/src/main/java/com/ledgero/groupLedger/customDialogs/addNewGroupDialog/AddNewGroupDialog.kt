package com.ledgero.groupLedger.customDialogs.addNewGroupDialog

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ledgero.R
import com.ledgero.databinding.DialogAddNewGroupBinding
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.CreateGroupButtonStates
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.CreatingGroupStates
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.SelectedMembersFlowState
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.flowStates.UserSearchFlowStates
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "AddNewGroupDialog"
private const val ADDED_TEXT = "ADDED"
private const val MAX_MEMBERS_SELECTED = "You have reached Max limit of group members"

@AndroidEntryPoint
class AddNewGroupDialog : DialogFragment() {
    private lateinit var _binding: DialogAddNewGroupBinding
    private val binding get() = _binding
    private val viewModel by viewModels<DialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogAddNewGroupBinding.inflate(inflater, container, false)


        setObservers()

        binding.searchUserBtn.setOnClickListener {
            val email = binding.userEmailSearchFieldTv.text.toString() + ""
            if (isValidEmail(email)) {

                viewModel.searchUser(email)

            } else {
                Toast.makeText(
                    context,
                    "Please type correct user Email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btCancelDialogFrag.setOnClickListener {
            dismiss()
        }

        binding.addFriendBtnUserFoundLayout.setOnClickListener {

            Log.d(TAG, "onCreateView: Adding New Member")
            viewModel.addCurrentSelectedUserInGroupMember()

        }

        binding.selectedMemberCancelBtn1.setOnClickListener(removeSelectedMemberClick)
        binding.selectedMemberCancelBtn2.setOnClickListener(removeSelectedMemberClick)
        binding.selectedMemberCancelBtn3.setOnClickListener(removeSelectedMemberClick)
        binding.selectedMemberCancelBtn4.setOnClickListener(removeSelectedMemberClick)


        binding.btMakeGroup.setOnClickListener {

            if (!binding.groupNameTv.text.isNullOrEmpty()) {
                val groupName = binding.groupNameTv.text.toString()
                viewModel.createGroup(groupName)
            } else Toast.makeText(context, "Please type group name", Toast.LENGTH_SHORT).show()


        }

        return binding.root
    }

    private fun setObservers() {
        userSearchFlowObserver()
        selectedMemberFlowObserver()
        createGroupButtonObserver()
        groupCreationObserver()
    }

    private fun groupCreationObserver() {
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.creatingNewGroupFlow.collect {
                    when (it) {
                        is CreatingGroupStates.Error -> Unit
                        CreatingGroupStates.Idle -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        CreatingGroupStates.Success -> {

                            Toast.makeText(context, "Group Created!", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                            dismiss()

                        }
                        CreatingGroupStates.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE

                        }
                    }
                }
            }
        }
    }

    private fun createGroupButtonObserver() {

        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createGroupBtnFlow.collect {
                    when (it) {
                        CreateGroupButtonStates.Disabled -> {
                            binding.btMakeGroup.apply {
                                isClickable = false
                                isEnabled = false
                            }
                        }
                        CreateGroupButtonStates.Enabled -> {
                            binding.btMakeGroup.apply {
                                isClickable = true
                                isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectedMemberFlowObserver() {
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.selectedMembersFlow.collect {
                    when (it) {
                        is SelectedMembersFlowState.Done -> {
                            binding.userFoundContainerDialogFrag.visibility = View.GONE
                        }
                        is SelectedMembersFlowState.LimitReached -> {
                            Toast.makeText(context, MAX_MEMBERS_SELECTED, Toast.LENGTH_SHORT).show()
                        }
                        is SelectedMembersFlowState.MemberAdded -> {
                            binding.membersAddedLayout.visibility = View.VISIBLE
                            Log.d(TAG, "selectedMemberFlowObserver: Member Added Flow Received")
                            addSelectedMember()
                            viewModel.newMemberAddedSuccessfully()
                        }
                        is SelectedMembersFlowState.MemberRemoved -> {
                            removeAllSelectedMembers()
                            addSelectedMember()
                            viewModel.newMemberAddedSuccessfully()
                        }
                    }
                }
            }
        }
    }

    private fun removeAllSelectedMembers() {
        binding.selectedMemberLayout1.visibility = View.GONE
        binding.selectedMemberLayout2.visibility = View.GONE
        binding.selectedMemberLayout3.visibility = View.GONE
        binding.selectedMemberLayout4.visibility = View.GONE
    }

    private fun addSelectedMember() {
        if (viewModel.totalMembersSelected <= 0) {
            Log.d(
                TAG,
                "addSelectedMember: ${viewModel.totalMembersSelected}  No Selected Member Found, could not show anything "
            )
            return
        }
        var index = 1
        for (i in viewModel.selectedGroupMembers) {
            Log.d(TAG, "addSelectedMember: Index = $index")
            when (index) {
                1 -> {
                    binding.selectedMemberLayout1.visibility = View.VISIBLE
                    binding.selectedMemberName1Tv.text = i.name
                    Log.d(TAG, "addSelectedMember: 1st selected member displayed ")

                }
                2 -> {
                    binding.selectedMemberLayout2.visibility = View.VISIBLE
                    binding.selectedMemberName2Tv.text = i.name
                    Log.d(TAG, "addSelectedMember: 2nd selected member displayed ")

                }
                3 -> {
                    binding.selectedMemberLayout3.visibility = View.VISIBLE
                    binding.selectedMemberName3Tv.text = i.name
                    Log.d(TAG, "addSelectedMember: 3rd selected member displayed ")

                }
                4 -> {
                    binding.selectedMemberLayout4.visibility = View.VISIBLE
                    binding.selectedMemberName4Tv.text = i.name
                    Log.d(TAG, "addSelectedMember: 4th selected member displayed ")

                }
            }
            index++
        }
    }

    private fun userSearchFlowObserver() {

        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.userSearchFlow.collect {
                    binding.progressBar.visibility = View.GONE

                    when (it) {

                        is UserSearchFlowStates.Idle -> {
                            Log.d(TAG, "userSearchFlowObserver: IDLE")
                            binding.progressBar.visibility = View.GONE
                            binding.userSearchInfoTv.visibility = View.GONE
                        }
                        is UserSearchFlowStates.Error -> {

                            Log.d(TAG, "userSearchFlowObserver: Error ${it.errorMessage}")
                            binding.progressBar.visibility = View.GONE
                            binding.userSearchInfoTv.text = it.errorMessage
                            binding.userSearchInfoTv.visibility = View.VISIBLE
                        }
                        UserSearchFlowStates.Loading -> {
                            Log.d(TAG, "userSearchFlowObserver: Loading")
                            binding.progressBar.visibility = View.VISIBLE
                            binding.userSearchInfoTv.visibility = View.GONE
                        }
                        is UserSearchFlowStates.Success -> {

                            Log.d(TAG, "userSearchFlowObserver: Success")
                            binding.progressBar.visibility = View.GONE
                            binding.userSearchInfoTv.visibility = View.GONE
                            viewModel.currentSearchedUser = it.groupMember
                            showFoundUser(it.groupMember)
                        }
                    }
                }
            }

        }
    }

    private fun showFoundUser(groupMember: Member) {

        binding.userFoundContainerDialogFrag.visibility = View.VISIBLE
        binding.userNameUserFoundLayout.text = groupMember.name
        binding.userEmailUserFoundLayout.text = groupMember.email

        if (viewModel.isMemberAlreadySelected(groupMember.uid)) {
            binding.addFriendBtnUserFoundLayout.apply {
                text = ADDED_TEXT
                isEnabled = false
                isClickable = false
                viewModel.currentSearchedUser = null
            }

        } else {
            Log.d(TAG, "showFoundUser: User Not Already Added")
            binding.addFriendBtnUserFoundLayout.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setTextColor(resources.getColor(R.color.white, null))
                }
                isEnabled = true
                isClickable = true
            }
        }

    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private val removeSelectedMemberClick = View.OnClickListener {
        when (it.id) {

            binding.selectedMemberCancelBtn1.id -> {
                viewModel.selectedMemberRemove(1)
            }
            binding.selectedMemberCancelBtn2.id -> {
                viewModel.selectedMemberRemove(2)

            }
            binding.selectedMemberCancelBtn3.id -> {
                viewModel.selectedMemberRemove(3)

            }
            binding.selectedMemberCancelBtn4.id -> {
                viewModel.selectedMemberRemove(4)

            }

        }
    }

}