package com.ledgero.groupLedger

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.databinding.FragmentGroupLedgersBinding
import com.ledgero.groupLedger.customDialogs.addNewGroupDialog.AddNewGroupDialog
import com.ledgero.groupLedger.flowStates.AllGroupsStateFlow
import com.ledgero.groupLedger.recyclerViews.GroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter


private const val TAG="GroupLedgerFragment"
@AndroidEntryPoint
class GroupLedgersFragment : Fragment() {

    private lateinit var _binding: FragmentGroupLedgersBinding
    private val binding: FragmentGroupLedgersBinding get()= _binding
    private val viewModel by viewModels<GroupLedgerFragmentViewModel>()
    private lateinit var adapter: GroupAdapter
   private lateinit var rv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding= FragmentGroupLedgersBinding.inflate(inflater,container,false)

        adapter= GroupAdapter(requireContext(),viewModel.allGroups)
         rv= binding.groupRV
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter
getAllGroups()

        val cashRegisterButton = binding.btCashRegisterGroupLedgersFrag
        cashRegisterButton.setOnClickListener{
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }



        binding.btAddNewGroup.setOnClickListener {
            val dialog = AddNewGroupDialog()
            dialog.isCancelable= false
            dialog.show(childFragmentManager,"AddNewGroupDialog")

        }

        return binding.root
    }

    private fun getAllGroups() {
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
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
                    when(it){
                        is AllGroupsStateFlow.NewGroupAdded ->{

                            !viewModel.isGroupExist(it.group)
                        }
                        is AllGroupsStateFlow.AllGroupsFetched -> true
                        is AllGroupsStateFlow.GroupRemoved -> viewModel.isGroupExist(it.group)
                        is AllGroupsStateFlow.GroupUpdated -> true
                    }
                }.collect{
                    Log.d(TAG, "allGroupsObserver: groups flow collected")
                    when(it){
                        is AllGroupsStateFlow.AllGroupsFetched -> {
                            Log.d(TAG, "allGroupsObserver: All Groups Fetched")
                          //  Toast.makeText(context, "Total ${it.groups.size} fetched", Toast.LENGTH_SHORT).show()
                            viewModel.allGroups=it.groups
                            viewModel.allGroups.addAll(it.groups)
                            adapter.notifyDataSetChanged()
//                            val newAdapter= GroupAdapter(requireContext(),it.groups)
//                            rv.adapter=newAdapter
                        }
                        is AllGroupsStateFlow.GroupRemoved ->  {
                            Toast.makeText(context, " ${it.group.groupName} removed", Toast.LENGTH_SHORT).show()
                        }
                        is AllGroupsStateFlow.GroupUpdated ->  {
                            Toast.makeText(context, " ${it.group.groupName} updated", Toast.LENGTH_SHORT).show()
                        }
                        is AllGroupsStateFlow.NewGroupAdded ->  {
                            Log.d(TAG, "allGroupsObserver: group added ${it.group.groupName}")

                            viewModel.allGroups.add(it.group)
                            rv.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
}





    override fun onPause() {
        super.onPause()
        viewModel.stopAllObservers()
    }

}