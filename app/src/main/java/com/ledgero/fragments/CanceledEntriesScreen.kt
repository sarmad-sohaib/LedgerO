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
import com.ledgero.daos.CanceledEntriesDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.R
import com.ledgero.Repositories.CanceledEntriesRepo
import com.ledgero.ViewModelFactories.CanceledEntriesViewModelFactory
import com.ledgero.ViewModels.CanceledEntriesViewModel
import com.ledgero.adapters.CanceledEntriesScreen_RVAdapter
import kotlinx.android.synthetic.main.fragment_canceled_entries_screen.view.*

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

        adapter = CanceledEntriesScreen_RVAdapter(requireContext(), ArrayList<Entries>(),viewModel,entryClick,ledgerUID)
        rv.adapter= adapter


        viewModel.getCanceledEntries().observe(viewLifecycleOwner, Observer{
            adapter = CanceledEntriesScreen_RVAdapter(requireContext(), it,viewModel,entryClick,ledgerUID)
            rv.adapter=adapter

        })



        view.deleteAll_btn_canceledScreen.setOnClickListener {

            (adapter as CanceledEntriesScreen_RVAdapter).deleteAll()

        }

        return view
    }

    override fun onDestroy() {
        viewModel.removeListener()
        super.onDestroy()
    }

    private val entryClick= fun ( frag:Fragment){
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment_container_main,frag)
            .addToBackStack(null)
            .commit()

    }
}