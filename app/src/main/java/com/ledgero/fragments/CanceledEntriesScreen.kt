package com.ledgero.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DAOs.CanceledEntriesDAO
import com.ledgero.DAOs.UnApproveEntriesDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.R
import com.ledgero.Repositories.CanceledEntriesRepo
import com.ledgero.Repositories.UnApprovedEntriesRepo
import com.ledgero.ViewModelFactories.CanceledEntriesViewModelFactory
import com.ledgero.ViewModelFactories.UnApprovedEntriesViewModelFactory
import com.ledgero.ViewModels.CanceledEntriesViewModel
import com.ledgero.ViewModels.UnApprovedEntriesViewModel
import com.ledgero.adapters.CanceledEntriesScreen_RVAdapter
import com.ledgero.adapters.UnApprovedEntries_RVAdapter

class CanceledEntriesScreen(var ledgerUID: String) : Fragment() {

    var currentSelectedLedgerUID:String=ledgerUID
    var currentSelectLedger: SingleLedgers? =null
    lateinit     var viewModel: CanceledEntriesViewModel

    private var layoutManager: RecyclerView.LayoutManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_canceled_entries_screen, container, false)



        var dao= CanceledEntriesDAO(ledgerUID)
        var repo = CanceledEntriesRepo(dao)
        viewModel= ViewModelProvider(this, CanceledEntriesViewModelFactory(repo))
            .get(CanceledEntriesViewModel::class.java)

        var rv = view.findViewById<RecyclerView>(R.id.rv_canceledScreen)

        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager
        var adapter: RecyclerView.Adapter<CanceledEntriesScreen_RVAdapter.CanceledEntries_ViewHolder>? = null

        adapter = CanceledEntriesScreen_RVAdapter(requireContext(), ArrayList<Entries>(),viewModel)
        rv.adapter= adapter


        viewModel.getCanceledEntries().observe(viewLifecycleOwner, Observer{
            adapter = CanceledEntriesScreen_RVAdapter(requireContext(), it,viewModel)
            rv.adapter=adapter

        })

        return view
    }

    override fun onDestroy() {
        viewModel.removeListener()
        super.onDestroy()
    }
}