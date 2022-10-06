package com.ledgero.cashregister.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ledgero.R
import com.ledgero.cashregister.CashRegisterEvents
import com.ledgero.cashregister.EntriesViewModel
import com.ledgero.cashregister.MyCallback
import com.ledgero.cashregister.data.Entry
import com.ledgero.cashregister.ui.entries.adapter.EntriesListAdapter
import com.ledgero.databinding.FragmentEntriesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "EntriesFragment"
private const val CASH_IN = "in"
private const val CASH_OUT = "out"

@AndroidEntryPoint
class EntriesFragment : Fragment(), EntriesListAdapter.OnItemClick {

    private lateinit var binding: FragmentEntriesBinding
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val viewModel: EntriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val adapter = EntriesListAdapter(this)
        val uId = FirebaseAuth.getInstance().currentUser?.uid
        binding = FragmentEntriesBinding.inflate(inflater)

        binding.apply {
            recyclerViewEntries.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewEntries.adapter = adapter
            recyclerViewEntries.setHasFixedSize(true)
            
            buttonCashIn.setOnClickListener { 
                viewModel.cashInButtonClicked()
            }
            
            buttonCashOut.setOnClickListener { 
                viewModel.cashOutButtonClicked()
            }

            //swipe to delete implementation
            ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val entry = adapter.currentList[viewHolder.adapterPosition]
                    entry.id?.let { id ->
                        viewModel.onEntrySwiped(uId!!,
                        id,
                        entry) }
                }

            }).attachToRecyclerView(recyclerViewEntries)
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        //Loading all entries of cash register into recyclerView
        /**
         * using [MyCallback] because otherwise the list of entries got from
         * repository will always be empty.
         */
        if (currentUserId != null) {
            viewModel.getEntries(currentUserId, object : MyCallback {
                override fun onCallback(value: List<Entry>) {
                    binding.recyclerViewEntries.adapter = adapter

                    var cashOutSum = 0
                    var cashInSum = 0
                    var availableCash = 0

                    for(result in value) {
                        if (result.out == true) cashOutSum += result.amount?.toInt() ?: 0
                        if (result.out == false) cashInSum += result.amount?.toInt() ?: 0
                    }

                    availableCash = cashInSum - cashOutSum
                    binding.tvAvailableCashAmount.text = availableCash.toString()

                    if (availableCash == 0) binding.buttonCashOut.isEnabled = false
                    if (availableCash < 0) binding.tvAvailableCashAmount.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                    else binding.tvAvailableCashAmount.setTextColor(ContextCompat.getColor(context!!, R.color.green))
                    adapter.submitList(value)
                }
            })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cashRegisterEvents.collect { event ->
                    when(event) {
                        is CashRegisterEvents.NavigateToCashOutEntryScreen -> {
                            val action =
                                EntriesFragmentDirections.actionEntriesFragmentToAddEditEntryFragment(
                                    CASH_OUT
                                )
                            findNavController().navigate(action)
                        }
                        
                        is CashRegisterEvents.NavigateToCashInEntryScreen -> {
                            val action =
                                EntriesFragmentDirections.actionEntriesFragmentToAddEditEntryFragment(
                                    CASH_IN
                                )
                            findNavController().navigate(action)
                        }

                        is CashRegisterEvents.ShowUndoDeletedEntryMessage -> {
                            Snackbar.make(requireView(), "Entry Deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    if (uId != null) {
                                        viewModel.onUndoDeleteClick(uId, event.entry)
                                    }
                                }.show()
                        }
                        
                        is CashRegisterEvents.NavigateToEditEntryScreen -> {
                            val action = EntriesFragmentDirections.actionEntriesFragmentToAddEditEntryFragment("", event.entry)
                            findNavController().navigate(action)
                        }

                        is CashRegisterEvents.ShowTaskSavedConfirmationMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onItemClick(entry: Entry) {
        viewModel.onEntrySelected(entry)
    }
}