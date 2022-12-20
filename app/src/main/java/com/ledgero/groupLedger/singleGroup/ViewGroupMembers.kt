package com.ledgero.groupLedger.singleGroup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.protobuf.Value
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.databinding.DialogAddNewGroupBinding
import com.ledgero.databinding.FragmentViewGroupMembersBinding
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.DialogViewModel
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.model.UtillFunctions
import com.ledgero.utils.GroupEntriesHelper
import com.ledgero.utils.GroupMembersInterface

private const val TAG = "ViewGroupMember"

class ViewGroupMembers(val groupInfo: GroupInfo) : Fragment() {
    private lateinit var _binding: FragmentViewGroupMembersBinding
    private val binding get() = _binding
    private var db_reference = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewGroupMembersBinding.inflate(inflater, container, false)

        if (User.userID== groupInfo.groupAdminUID){
            binding.addMemberViewMembers.visibility= View.VISIBLE
        }

        binding.addMemberViewMembers.setOnClickListener{

            val dialog = AddNewMemberInExistingGroup(groupInfo)
            dialog.isCancelable = false
            dialog.show(childFragmentManager, "addMemberInExisting")

        }

        binding.okBtnViewMembers.setOnClickListener {
            parentFragmentManager.popBackStack()
        }





        return binding.root
    }

    private fun setListener(){
        db_reference.child("groupsInfo").child(groupInfo.groupUID).child("groupMembers")
            .addValueEventListener(listener)
    }
    fun removeListener(){
        db_reference.child("groupsInfo").child(groupInfo.groupUID).child("groupMembers")
            .removeEventListener(listener)
    }
    override fun onResume() {
setListener()
        super.onResume()
    }

    override fun onPause() {
        removeListener()
        super.onPause()
    }

    private val listener= object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {

            hideAllUserLayouts()
            if (snapshot.exists()){
                val members= ArrayList<Member>()
                snapshot.children.forEach{
                    val m= it.getValue(Member::class.java)!!
                    members.add(m)
                }
                groupInfo.groupMembers=members
                binding.addMemberViewMembers.isEnabled = members.size<3

                for (i in 0 until members.size){
                    when(i){
                        0 ->{
                            binding.member2Layout.visibility= View.VISIBLE
                            binding.member2Name.text= members[i].name
                            binding.member2Email.text= members[i].email
                            if (User.userID== groupInfo.groupAdminUID){
                            binding.member2LeaveBtn.setOnClickListener{
                              leaveMember(groupInfo,members[i].uid)
                            }
                            }else{
                                binding.member2LeaveBtn.setOnClickListener{
                                    Toast.makeText(context, "Only Admin can remove the member", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                        1 ->{
                            binding.member3Layout.visibility= View.VISIBLE
                            binding.member3Name.text= members[i].name
                            binding.member3Email.text= members[i].email
                            if (User.userID== groupInfo.groupAdminUID){
                                binding.member3LeaveBtn.setOnClickListener{
                                    leaveMember(groupInfo,members[i].uid)
                                }
                            }else{
                                binding.member3LeaveBtn.setOnClickListener{
                                    Toast.makeText(context, "Only Admin can remove the member", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        2 ->{
                            binding.member4Layout.visibility= View.VISIBLE
                            binding.member4Name.text= members[i].name
                            binding.member4Email.text= members[i].email
                            if (User.userID== groupInfo.groupAdminUID){
                                binding.member4LeaveBtn.setOnClickListener{
                                    leaveMember(groupInfo,members[i].uid)
                                }
                        }else{
                                binding.member4LeaveBtn.setOnClickListener{
                                    Toast.makeText(context, "Only Admin can remove the member", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }


            }
        }else{
                groupInfo.groupMembers=ArrayList()
                binding.addMemberViewMembers.isEnabled = true

            }

            binding.member1Layout.visibility= View.VISIBLE
            binding.member1Name.text= groupInfo.groupAdminName +" (Admin)"
            binding.member1Email.text= groupInfo.groupAdminEmail
            if (User.userID== groupInfo.groupAdminUID){
                binding.member1LeaveBtn.setOnClickListener{
                    Toast.makeText(context, "Admin cannot be removed", Toast.LENGTH_SHORT).show()

                }
            }else{
                binding.member1LeaveBtn.setOnClickListener{
                    Toast.makeText(context, "Only Admin can remove the member", Toast.LENGTH_SHORT).show()
                }
            }


        }

        override fun onCancelled(error: DatabaseError) = Unit

    }

    fun leaveMember(groupInfo: GroupInfo,uid:String){
        val progress= UtillFunctions.setProgressDialog(requireContext(),"Removing Member")
        UtillFunctions.showProgressDialog(progress)
        GroupEntriesHelper.leaveGroup(groupInfo,uid, object : GroupMembersInterface{
            override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                UtillFunctions.hideProgressDialog(progress )
                if (isSuccessFull){
                Toast.makeText(requireContext(), "Member Removed", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Could not remove member", Toast.LENGTH_SHORT).show()

                }
            }

        })
    }

    fun hideAllUserLayouts(){
        binding.member1Layout.visibility= View.GONE
        binding.member2Layout.visibility= View.GONE
        binding.member3Layout.visibility= View.GONE
        binding.member4Layout.visibility= View.GONE
    }

}