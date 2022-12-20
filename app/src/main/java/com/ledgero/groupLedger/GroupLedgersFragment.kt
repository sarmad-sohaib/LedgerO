package com.ledgero.groupLedger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.ViewReportActivity
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.databinding.FragmentGroupLedgersBinding
import com.ledgero.databinding.GroupLedgerMainFragmentBinding
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.AddNewGroupDialog
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.groupLedger.flowStates.AllGroupsStateFlow
import com.ledgero.groupLedger.recyclerViews.GroupAdapter
import com.ledgero.groupLedger.singleGroup.SingleGroupScreen
import com.ledgero.model.UtillFunctions
import com.ledgero.utils.AllGroupsAmount
import com.ledgero.utils.GroupEntriesHelper
import com.ledgero.utils.GroupMembersInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter


private const val TAG = "GroupLedgerFragment"

@AndroidEntryPoint
class GroupLedgersFragment : Fragment() {

    private lateinit var _binding: GroupLedgerMainFragmentBinding
    private lateinit var binding: GroupLedgerMainFragmentBinding
    private val viewModel by viewModels<GroupLedgerFragmentViewModel>()
    private lateinit var adapter: GroupAdapter
    private lateinit var rv: RecyclerView
    private val dbReference = FirebaseDatabase.getInstance().reference
    private var userGroupsList = ArrayList<GroupInfo>()
    private lateinit var totalInText: TextView
    private lateinit var totalOutText: TextView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //getAllGroups()

        rv = binding.groupRV


        rv.layoutManager = LinearLayoutManager(context)
        getTouchHelper(rv).attachToRecyclerView(rv)

        getUserGroups()

        val cashRegisterButton = binding.btCashRegisterGroupLedgersFrag
        cashRegisterButton.setOnClickListener {
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }



        binding.btAddNewGroup.setOnClickListener {
            val dialog = AddNewGroupDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, "AddNewGroupDialog")

        }


    }

    private fun getUserGroups() {
        binding.progressBarGroupLedger.visibility = View.VISIBLE
        dbReference.child("users").child(User.userID.toString()).child("user_group_Ledgers")
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        userGroupsList.clear()
                        viewModel.allGroups.clear()
                        val groupsList = ArrayList<GroupInfo>()
                        snapshot.children.forEach {
                            val g = it.getValue(GroupInfo::class.java)!!
                            Log.d(TAG, "getAllGroups: ${g.toString()}")
                            groupsList.add(g)
                        }
                        userGroupsList = groupsList
                        adapter = GroupAdapter(requireContext(), groupsList, singleGroupClick)
                        rv.adapter = adapter
                        viewModel.allGroups=userGroupsList
                        binding.totalCashIn.text= "Rs.00"
                        binding.totalCashOut.text= "Rs.00"
                        viewModel.showAllGroupsAmount(object : AllGroupsAmount{
                            override fun onAllGroupsAmount() {

                                updateUI()
                                binding.progressBarGroupLedger.visibility = View.GONE

                            }

                        })


                    } else {
                        adapter = GroupAdapter(requireContext(), ArrayList(), singleGroupClick)
                        rv.adapter = adapter

                        binding.progressBarGroupLedger.visibility = View.GONE

                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })


        binding.tvPaymentHistoryMoneyFrag.setOnClickListener {
            val m = Intent(requireContext(), ViewReportActivity::class.java)
            m.putExtra("isGroup", true)

            startActivity(m)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = GroupLedgerMainFragmentBinding.inflate(inflater,container,false)
        binding= _binding

        return binding.root
    }

    private fun updateUI() {

        binding.totalCashIn.text= "Rs. "+viewModel.totalIn.toString()
        binding.totalCashOut.text= "Rs. "+viewModel.totalOut.toString()
        adapter = GroupAdapter(requireContext(), viewModel.allGroups, singleGroupClick)
        rv.adapter = adapter
    }

    private fun getAllGroups() {
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "onCreateView: resumed")
                allGroupsObserver()
                viewModel.getAllGroups()

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun allGroupsObserver() {
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.allGroupsListenerFlow.filter {
                    when (it) {
                        is AllGroupsStateFlow.NewGroupAdded -> {

                            !viewModel.isGroupExist(it.group)
                        }
                        is AllGroupsStateFlow.AllGroupsFetched -> true
                        is AllGroupsStateFlow.GroupRemoved -> viewModel.isGroupExist(it.group)
                        is AllGroupsStateFlow.GroupUpdated -> true
                    }
                }.collect {
                    Log.d(TAG, "allGroupsObserver: groups flow collected")
                    when (it) {
                        is AllGroupsStateFlow.AllGroupsFetched -> {
                            Log.d(TAG, "allGroupsObserver: All Groups Fetched")
                            //  Toast.makeText(context, "Total ${it.groups.size} fetched", Toast.LENGTH_SHORT).show()

                            adapter.notifyDataSetChanged()
//                            val newAdapter= GroupAdapter(requireContext(),it.groups)
//                            rv.adapter=newAdapter
                        }
                        is AllGroupsStateFlow.GroupRemoved -> {
                            Toast.makeText(
                                context,
                                " ${it.group.groupName} removed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is AllGroupsStateFlow.GroupUpdated -> {
                            Toast.makeText(
                                context,
                                " ${it.group.groupName} updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is AllGroupsStateFlow.NewGroupAdded -> {
                            Log.d(TAG, "allGroupsObserver: group added ${it.group.groupName}")

                     //       viewModel.allGroups.add(it.group)
                            rv.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }


    val singleGroupClick = fun(groupInfo: GroupInfo) {
        var frag = SingleGroupScreen(groupInfo)

        Log.d(TAG, "${groupInfo.toString()}: ")

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fl_fragment_container_main, frag)
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopAllObservers()
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


                    getDeleteDialogueBox(position, rv).show()
                    rv.adapter!!.notifyDataSetChanged()


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
    fun getDeleteDialogueBox(pos: Int, rv: RecyclerView): AlertDialog.Builder {

        val dialog = AlertDialog.Builder(this.context)
        var title = "Leave Group "
        var message = "Are you sure to leave this Group!"

        if (userGroupsList.get(pos).groupAdminUID == User.userID!!) {
            title = "Are you sure to leave this Group!"
            message =
                "You are admin of group.\n If you decide to leave it then this group will be deleted permanently, and all members will be removed"
        }


        dialog.setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Yes Leave it") { _, _ ->
                //delete group


                val progress =
                    UtillFunctions.setProgressDialog(requireContext(), "Leaving Group...")
                UtillFunctions.showProgressDialog(progress)
                if (userGroupsList[pos].groupAdminUID == User.userID!!){


                    GroupEntriesHelper.deleteGroupForAll(userGroupsList[pos], User.userID!!, object :
                        GroupMembersInterface {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                            UtillFunctions.hideProgressDialog(progress)
                            rv.adapter!!.notifyDataSetChanged()
                            if (isSuccessFull) {
                                showAllGroupsAmount()
                                Toast.makeText(context, "Group Removed", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Something went wrong. Can't remove group",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                    })


                }else{

                    GroupEntriesHelper.leaveGroup(userGroupsList[pos], User.userID!!, object :
                        GroupMembersInterface {
                        override fun onGroupMembersUpdated(isSuccessFull: Boolean) {
                            UtillFunctions.hideProgressDialog(progress)
                            rv.adapter!!.notifyDataSetChanged()
                            if (isSuccessFull) {
                                Toast.makeText(context, "Group Removed", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Something went wrong. Can't remove group",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                    })
                }


            }
            .setNegativeButton("No") { dialogInterface, _ ->
                //cancel it
                dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showAllGroupsAmount(){

                viewModel.showAllGroupsAmount(object : AllGroupsAmount{
                    override fun onAllGroupsAmount() {

                        updateUI()

                    }

                })



    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        getUserGroups()
    }


}