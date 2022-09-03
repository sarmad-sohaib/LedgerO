package com.ledgero.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.adapters.RecyclerAdapter_SingleLedger
import com.ledgero.DAOs.IndividualScreenDAO
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.ViewModelFactories.IndividualScreenViewModeFactory
import com.ledgero.ViewModels.IndividualScreenViewModel
import kotlinx.android.synthetic.main.fragment_individual_ledger_screen.view.*

class IndividualLedgerScreen(ledgerUID:String) : Fragment() {

     var currentSelectedLedgerUID:String
    var currentSelectLedger: SingleLedgers? =null
lateinit     var viewModel: IndividualScreenViewModel

    private var layoutManager: RecyclerView.LayoutManager? = null



    init {
        currentSelectedLedgerUID=ledgerUID




        for (i in User.getUserSingleLedgers()!!){

            if (i.ledgerUID.equals(ledgerUID)){
                currentSelectLedger=i

            }
        }
        instanceObject=this

    }

    companion object{
        var adapter: RecyclerView.Adapter<RecyclerAdapter_SingleLedger.MyViewHolder>? = null
        var instanceObject: IndividualLedgerScreen? =null;

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_individual_ledger_screen, container, false)

        //View Model Init

        var dao= IndividualScreenDAO(currentSelectedLedgerUID)
        var repo = IndividualScreenRepo(dao)
        viewModel= ViewModelProvider(this, IndividualScreenViewModeFactory(repo))
            .get(IndividualScreenViewModel::class.java)



        //View Model Init ---Ends Here

        var gotButton= view.bt_got_individScreen
        var gaveButton= view.bt_gave_individScreen
        var rv = view.findViewById<RecyclerView>(R.id.rv_ledgers_individualScreen)
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager
        adapter= RecyclerAdapter_SingleLedger(requireContext(),currentSelectLedger!!.entries)
        rv.adapter= adapter

        viewModel.getEntries().observe(viewLifecycleOwner, Observer{

            currentSelectLedger!!.entries=it /* = java.util.ArrayList<com.ledgero.DataClasses.Entries> */
            adapter= RecyclerAdapter_SingleLedger(requireContext(),currentSelectLedger!!.entries)
            rv.adapter= adapter
        })
        gotButton.setOnClickListener(){
            //1 will inidcate that user clicked got button
          setFragmentResult("addEntryBtn", bundleOf("mode" to 1,"ledger" to currentSelectLedger))
           parentFragmentManager
               .beginTransaction()
               .addToBackStack(null)
               .replace(R.id.fl_fragment_container_main,AddNewEntryDetail())
               .commit()

        }
        gaveButton.setOnClickListener(){
            //0 will indicate that user clicked gave button
            setFragmentResult("addEntryBtn", bundleOf("mode" to 0,"ledger" to currentSelectLedger))
            parentFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fl_fragment_container_main,AddNewEntryDetail())
                .commit()

        }


   return view
    }


    override fun onDestroy() {
        viewModel.removeListener()
        super.onDestroy()
    }

    override fun onResume() {
        parentFragmentManager.clearBackStack("addEntry")
        super.onResume()
    }
}