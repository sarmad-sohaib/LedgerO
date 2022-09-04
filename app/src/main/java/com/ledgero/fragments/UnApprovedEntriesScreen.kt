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
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.DAOs.IndividualScreenDAO
import com.ledgero.DAOs.UnApproveEntriesDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.R
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.Repositories.UnApprovedEntriesRepo
import com.ledgero.ViewModelFactories.IndividualScreenViewModeFactory
import com.ledgero.ViewModelFactories.UnApprovedEntriesViewModelFactory
import com.ledgero.ViewModels.IndividualScreenViewModel
import com.ledgero.ViewModels.UnApprovedEntriesViewModel
import com.ledgero.adapters.RecyclerAdapter_SingleLedger
import com.ledgero.adapters.UnApprovedEntries_RVAdapter

class UnApprovedEntriesScreen(var ledgerUID: String) : Fragment() {
    var currentSelectedLedgerUID:String=ledgerUID
    var currentSelectLedger: SingleLedgers? =null
    lateinit     var viewModel: IndividualScreenViewModel

    private var layoutManager: RecyclerView.LayoutManager? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
    val view = inflater.inflate(R.layout.fragment_un_approved_entries_screen, container, false)


        var dao= UnApproveEntriesDAO(ledgerUID)
        var repo = UnApprovedEntriesRepo(dao)
         var viewModel= ViewModelProvider(this, UnApprovedEntriesViewModelFactory(repo))
            .get(UnApprovedEntriesViewModel::class.java)



        var rv = view.findViewById<RecyclerView>(R.id.rv_unapprovedScreen)

        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager
        var adapter: RecyclerView.Adapter<UnApprovedEntries_RVAdapter.UnApprovedEntries_ViewHolder>? = null

       adapter = UnApprovedEntries_RVAdapter(requireContext(), ArrayList<Entries>())
        rv.adapter= adapter

        viewModel.getEntries().observe(viewLifecycleOwner, Observer{
            adapter = UnApprovedEntries_RVAdapter(requireContext(), it)
            rv.adapter=adapter

        })


    return view
    }
}
