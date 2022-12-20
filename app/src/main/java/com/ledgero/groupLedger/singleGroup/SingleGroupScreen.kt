package com.ledgero.groupLedger.singleGroup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.GroupLedgersInfo
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.databinding.FragmentSingleGroupScreenBinding
import com.ledgero.fragments.AddNewEntryDetail
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.AddNewGroupDialog
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.data.Member
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.other.Constants
import com.ledgero.other.Constants.CASH_IN
import com.ledgero.pushnotifications.PushNotification
import com.ledgero.utils.GroupEntriesHelper
import java.math.RoundingMode
import kotlin.math.absoluteValue


private const val TAG= "SingleGroupScreen"
class SingleGroupScreen(private val groupInfo: GroupInfo) : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var _binding: FragmentSingleGroupScreenBinding
    private val binding: FragmentSingleGroupScreenBinding get() = _binding
    private val db = FirebaseDatabase.getInstance().reference
    private var groupEntries= ArrayList<GroupEntry>()

    private lateinit var rv : RecyclerView
    companion object {
        var adapter: RecyclerView.Adapter<SingleGroupAdapter.ViewHolder>? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleGroupScreenBinding.inflate(inflater, container, false)

        layoutManager = LinearLayoutManager(context)
        rv = binding.rvLedgersSingleGroup
        rv.layoutManager = layoutManager

        getTouchHelper(rv).attachToRecyclerView(rv)

        binding.progressBarSingleGroup.visibility= View.VISIBLE

        db.child("groupEntries").child(groupInfo.groupUID).addValueEventListener(groupListener)


        Log.d(TAG, "onCreateView: User ${User.userID}  && ${groupInfo.groupUID}")

        if (User.userID == groupInfo.groupAdminUID){
            binding.btCashInSingleGroup.visibility= View.VISIBLE
            binding.btCashOutSingleGroup.visibility= View.VISIBLE
        }



        binding.btCashInSingleGroup.setOnClickListener {
            //1 will inidcate that user clicked got button
            setFragmentResult("addEntryBtn", bundleOf("mode" to Constants.CASH_IN,"ledger" to "currentSelectLedger"))
            parentFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fl_fragment_container_main, AddNewEntryDetail(null,true,groupInfo))
                .commit()

        }
        binding.btCashOutSingleGroup.setOnClickListener {
            //0 will indicate that user clicked gave button
            setFragmentResult("addEntryBtn", bundleOf("mode" to Constants.CASH_OUT,"ledger" to "currentSelectLedger"))
            parentFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fl_fragment_container_main, AddNewEntryDetail(null,true,groupInfo))
                .commit()

        }

        binding.groupInfoBtnSingleGroup.setOnClickListener{
            val dialog = GroupInfoDialog(groupInfo)
            dialog.isCancelable = false
            dialog.show(childFragmentManager, "groupInfo")

        }
        binding.viewMemberBtnSingleGroup.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fl_fragment_container_main, ViewGroupMembers(groupInfo))
                .commit()
        }



        return binding.root
    }

    //this will enable left/right swipes on RV
    private fun getTouchHelper(rv: RecyclerView): ItemTouchHelper {

        val callBack: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.
        SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT) {

                    if (groupInfo.groupAdminUID!= User.userID){

                        Toast.makeText(context, "Only Group Admin Can Delete an Entry", Toast.LENGTH_LONG)
                            .show()
                        rv.adapter!!.notifyDataSetChanged()
                    }else{
                    getDeleteDialogueBox(position, rv).show()
                    rv.adapter!!.notifyDataSetChanged()
                    }

                }
                if (direction == ItemTouchHelper.RIGHT) {


                    Toast.makeText(context, "Swipe Left to delete the LEdger", Toast.LENGTH_SHORT)
                        .show()
                    rv.adapter!!.notifyDataSetChanged()

                }

            }


        }


        return ItemTouchHelper(callBack)

    }
    @SuppressLint("NotifyDataSetChanged")
    fun getDeleteDialogueBox(pos: Int, rv: RecyclerView): AlertDialog.Builder{

        val dialog= AlertDialog.Builder(this.context)

        dialog.setTitle("Deleting Ledger ")
            .setMessage("Are you sure to delete this entry!")
            .setCancelable(true)
            .setPositiveButton("Yes Delete it"){ _, _ ->
                //delete ledger


                db.child("groupEntries").child(groupInfo.groupUID).child(groupEntries[pos].entryUID!!).removeValue()
                 .addOnCompleteListener {
                        if (it.isSuccessful) {

                            Log.d(TAG, "getDeleteDialogueBox: Entry deleted ")
                            rv.adapter!!.notifyDataSetChanged()
                           GroupEntriesHelper.sendNotificationToAllMember(groupInfo, Constants.ENTRY_REMOVED_GROUP)

                        }
                    }


            }
            .setNegativeButton("No"){ dialogInterface, _ ->
                //cancel it
                dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
        return dialog
    }


    private val groupListener = object : ValueEventListener{
        @SuppressLint("SetTextI18n")
        override fun onDataChange(it: DataSnapshot) {

            if (it.exists()){
                val entries= ArrayList<GroupEntry>()
                var totalCashIn=0f
                var totalCashOut=0f
                it.children.forEach{
                    val e = it.getValue(GroupEntry::class.java)!!
                    entries.add(e)
                    if (e.cashInOutFlag==CASH_IN) totalCashIn+=e.amount!!else totalCashOut+=e.amount!!

                }
                groupEntries= entries


                try{

                    adapter = SingleGroupAdapter(requireContext(), entries, groupInfo)
                    rv.adapter = adapter

                }catch (e:Exception){
                    Log.d(TAG, "onDataChange: ${e.message}")
                }   
                var totalAmount =0f

                if (totalCashIn>totalCashOut) {
                    totalAmount= totalCashIn-totalCashOut
                    binding.tvCashInSingleGroup.text =
                        "Rs." + totalAmount.toBigDecimal().setScale(2, RoundingMode.UP).toDouble().toString()
                }
                if (totalCashIn<totalCashOut) {
                    totalAmount= totalCashOut-totalCashIn

                    binding.tvCashOutSingleGroup.text =
                        "Rs." + totalAmount.toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
                            .toString()
                }
                if (totalCashIn==totalCashOut){binding.tvCashOutSingleGroup.text= "Rs.00"
                    binding.tvCashInSingleGroup.text= "Rs.00"
                }
                binding.progressBarSingleGroup.visibility= View.GONE


            }else{
                binding.tvCashOutSingleGroup.text= "Rs.00"
                binding.tvCashInSingleGroup.text= "Rs.00"
             try {
                 adapter = SingleGroupAdapter(requireContext(), ArrayList(), groupInfo)
                 rv.adapter = adapter
             }
             catch (e:Exception){
                 Log.d(TAG, "onDataChange: ${e.message}")
             }
            }



            binding.progressBarSingleGroup.visibility= View.GONE

        }

        override fun onCancelled(error: DatabaseError)= Unit

    }

    override fun onResume() {
        db.child("groupEntries").child(groupInfo.groupUID).addValueEventListener(groupListener)

        super.onResume()
    }

    override fun onPause() {
        db.child("groupEntries").child(groupInfo.groupUID).removeEventListener(groupListener)

        super.onPause()
    }

}