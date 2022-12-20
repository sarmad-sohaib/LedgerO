package com.ledgero.groupLedger.singleGroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.databinding.AddNewInExistingGroupDialogBinding
import com.ledgero.databinding.GroupInfoDialogLayoutBinding
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.model.UtillFunctions
import com.ledgero.utils.AddNewMember
import com.ledgero.utils.GroupEntriesHelper
import com.ledgero.utils.GroupMembersInterface
import com.ledgero.utils.TimeFormatter
import io.grpc.okhttp.internal.Util
import java.util.*


private const val TAG = "AddInExisting"

class AddNewMemberInExistingGroup(val groupInfo: GroupInfo) : DialogFragment() {
    private lateinit var _binding: AddNewInExistingGroupDialogBinding
    private val binding get() = _binding
    private var db_reference = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AddNewInExistingGroupDialogBinding.inflate(inflater, container, false)

        binding.searchUser.setOnClickListener {


            hideError()
            hideUser()
            val email = binding.tvEmailDialogFrag.text.toString()
            if (email == groupInfo.groupAdminEmail){
                showError("You are already a member")
                return@setOnClickListener
            }
            if (GroupEntriesHelper.isValidEmail(email)) {

                val progress= UtillFunctions.setProgressDialog(requireContext(), "Searching User")
                UtillFunctions.showProgressDialog(progress)
                GroupEntriesHelper.searchUser(email, object : AddNewMember {
                    override fun onUserSearch(isSuccessFull: Boolean, theMember: Member?) {
                        UtillFunctions.hideProgressDialog(progress)
                        if (isSuccessFull) {

                            showUser(theMember)

                        } else {
                            showError("No such user found")
                            Toast.makeText(
                                requireContext(),
                                "No such user found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                })


            } else {
                showError("Please type correct email of member to search")
            }

        }

        binding.btCancelDialogFrag.setOnClickListener {
            dismiss()
        }


        return binding.root
    }

    private fun showUser(theMember: Member?) {
        if (theMember != null) {

            binding.userEmailUserFoundLayout.text = theMember.email
            binding.userNameUserFoundLayout.text = theMember.name
            binding.userFoundContainerDialogFrag.visibility = View.VISIBLE

            var flag= false
            groupInfo.groupMembers!!.forEach{
                if (it.uid== theMember.uid){
                    binding.addFriendBtnUserFoundLayout.text="Member Exists"
                    binding.addFriendBtnUserFoundLayout.isEnabled=false
                    flag=true
                }
            }


            if (!flag){
                binding.addFriendBtnUserFoundLayout.text="Add Member"
                binding.addFriendBtnUserFoundLayout.isEnabled=true

                binding.addFriendBtnUserFoundLayout.setOnClickListener {
                val p = UtillFunctions.setProgressDialog(requireContext(),"Adding Member in Group")
                UtillFunctions.showProgressDialog(p)
                GroupEntriesHelper.addNewMemberInExistedGroup(
                    groupInfo,
                    theMember,
                    object : GroupMembersInterface {
                        override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                            UtillFunctions.hideProgressDialog(p)
                            if (isSuccessFull) {
                                Toast.makeText(requireContext(), "Member Added", Toast.LENGTH_SHORT)
                                    .show()
                                dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Sorry! Can't add user. Please try again later",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                    })
            }
            }
        }

    }

    private fun hideUser(){

        binding.userEmailUserFoundLayout.text = ""
        binding.userNameUserFoundLayout.text = ""
        binding.userFoundContainerDialogFrag.visibility = View.GONE


    }
    fun showError(message: String) {
        binding.infoTvDialogFrag.text = "Please type correct email of user"
        binding.infoTvDialogFrag.visibility = View.VISIBLE
    }

    fun hideError() {
        binding.infoTvDialogFrag.visibility = View.GONE

    }
}