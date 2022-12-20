package com.ledgero.groupLedger.singleGroup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.databinding.DialogAddNewGroupBinding
import com.ledgero.databinding.GroupInfoDialogLayoutBinding
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.DialogViewModel
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.utils.GroupEntriesHelper
import com.ledgero.utils.TimeFormatter
import kotlinx.android.synthetic.main.fragment_more.view.*
import java.util.*

private const val TAG= "GroupInfo"
class GroupInfoDialog(val groupInfo: GroupInfo) : DialogFragment() {
    private lateinit var _binding: GroupInfoDialogLayoutBinding
    private val binding get() = _binding
    private var db_reference = FirebaseDatabase.getInstance().reference



    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = GroupInfoDialogLayoutBinding.inflate(inflater, container, false)

        showError("Fetching Group Details..Please wait!")
       db_reference.child("groupsInfo").child(groupInfo.groupUID)
           .get().addOnCompleteListener {
               if (it.isSuccessful){
                   if (it.result.exists()){

                       hideError()
                       val info = it.result.getValue(GroupInfo::class.java)!!
                       binding.groupNameTvGroupInfo.text= info.groupName
                       binding.adminNameTvGroupInfo.text= info.groupAdminName
                       binding.adminEmailTvGroupInfo.text= info.groupAdminEmail
                       binding.groupMembersTvGroupInfo.text = "${info.groupMembers!!.size+1}"
                       try {
                           val date= Date(info.groupCreateServerTimestamp.toLong())
                           binding.createdTvGroupInfo.text= TimeFormatter.getFormattedTime(date.toString(),date)

                       }catch (e:Exception){
                           binding.createdTvGroupInfo.text= "Could Not Fetch Created Time"

                       }

                   }else{
                       showError("Sorry! Could Not Fetch Group Info. Please Try Again Later")
                       Log.d(TAG, "onCreateView: group info does not exist")}

               }else{
                   showError("Sorry! Could Not Fetch Group Info. Please Try Again Later")
                   Log.d(TAG, "onCreateView: cant fetch group info")
               }
           }

        binding.okBtnGroupInfo.setOnClickListener{
            dismiss()
        }


        return binding.root
    }

    private fun showError(message:String){
        binding.infoBoxGroupInfo.visibility= View.GONE
        binding.errorGroupInfo.text= message
        binding.errorGroupInfo.visibility=View.VISIBLE

    }
    private fun hideError(){
        binding.infoBoxGroupInfo.visibility= View.VISIBLE
        binding.errorGroupInfo.visibility=View.GONE

    }
}