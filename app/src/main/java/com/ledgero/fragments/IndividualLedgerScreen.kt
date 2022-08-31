package com.ledgero.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.onIndividualLedgerScreenData
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.adapters.RecyclerAdapter_SingleLedger
import com.ledgero.adapters.RecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_individual_ledger_screen.view.*
import androidx.fragment.app.FragmentManager as FragmentManager

class IndividualLedgerScreen : Fragment(),onIndividualLedgerScreenData {

    lateinit var currentSelectedLedgerName:String
    var currentSelectLedger: SingleLedgers? =null

    private var layoutManager: RecyclerView.LayoutManager? = null

    companion object{
        var adapter: RecyclerView.Adapter<RecyclerAdapter_SingleLedger.MyViewHolder>? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_individual_ledger_screen, container, false)

        var gotButton= view.bt_got_individScreen
        var gaveButton= view.bt_gave_individScreen
        var rv = view.findViewById<RecyclerView>(R.id.rv_ledgers_individualScreen)
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager
adapter= RecyclerAdapter_SingleLedger(requireContext(),currentSelectLedger!!.getAllEntries())
rv.adapter= adapter
        gotButton.setOnClickListener(){
            //1 will inidcate that user clicked got button
          setFragmentResult("addEntryBtn", bundleOf("mode" to 1,"ledger" to currentSelectLedger))
            MainActivity.getMainActivityInstance().setFragment(AddNewEntryDetail(),true,"addNewEntry")

        }
        gaveButton.setOnClickListener(){
            //0 will indicate that user clicked gave button
            setFragmentResult("addEntryBtn", bundleOf("mode" to 0,"ledger" to currentSelectLedger))
            MainActivity.getMainActivityInstance().setFragment(AddNewEntryDetail(),true,"addNewEntry")

        }


   return view
    }

    override fun data(ledgerUID: String) {
       currentSelectedLedgerName=ledgerUID

        for (i in User.getUserSingleLedgers()!!){

            if (i.ledgerUID.equals(ledgerUID)){
                currentSelectLedger=i
            }
        }

    }



}