package com.ledgero.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DAOs.IndividualScreenDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.ViewModelFactories.IndividualScreenViewModeFactory
import com.ledgero.ViewModels.IndividualScreenViewModel
import com.ledgero.adapters.RecyclerAdapter_SingleLedger
import kotlinx.android.synthetic.main.fragment_individual_ledger_screen.view.*

class IndividualLedgerScreen(ledgerUID:String) : Fragment() {

     var currentSelectedLedgerUID:String
    var currentSelectLedger: SingleLedgers? =null
lateinit     var viewModel: IndividualScreenViewModel
lateinit var entries: ArrayList<Entries>

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
        savedInstanceState: Bundle?,
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

        getTouchHelper(rv).attachToRecyclerView(rv)
        viewModel.getEntries().observe(viewLifecycleOwner, Observer{

            currentSelectLedger!!.entries=it /* = java.util.ArrayList<com.ledgero.DataClasses.Entries> */
            adapter= RecyclerAdapter_SingleLedger(requireContext(),currentSelectLedger!!.entries)
            rv.adapter= adapter
            entries=it
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




        var unApprovedBtn=view.bt_unapproved_entriest_individScreen
        var canceledEntries=view.bt_canceled_entries_individScreen

        canceledEntries.setOnClickListener {
            var frag = CanceledEntriesScreen(currentSelectedLedgerUID)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fl_fragment_container_main,frag)
                .addToBackStack(null)
                .commit()
        }


        unApprovedBtn.setOnClickListener(){

            var frag= UnApprovedEntriesScreen(currentSelectedLedgerUID)

            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fl_fragment_container_main,frag)
                .addToBackStack(null)
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


    //this will enable left/right swipes on RV
    fun getTouchHelper(rv: RecyclerView):ItemTouchHelper{

        var callBack:ItemTouchHelper.SimpleCallback =object :ItemTouchHelper.
        SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
  return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                var position= viewHolder.adapterPosition
if (direction==ItemTouchHelper.LEFT){

    getDeleteDialougeBox(position,rv).show()

}
                if (direction==ItemTouchHelper.RIGHT){

                    getEditDialougeBox(position,rv).show()
                }

            }


        }

var itemTouchHelper: ItemTouchHelper= ItemTouchHelper(callBack)


return itemTouchHelper

    }


    fun getDeleteDialougeBox(pos: Int, rv: RecyclerView): AlertDialog.Builder{

       var dialog= AlertDialog.Builder(this.context)

        dialog.setTitle("Deleting Ledger Entry")
            .setMessage("Are you sure to delete entry!")
            .setCancelable(true)
            .setPositiveButton("Yes Delete it"){dialogInterface,it->
                //delete entry

                var entry= entries.get(pos)
                if (entry.requestMode==0){
                    viewModel.deleteEntry(pos)
                    dialogInterface.dismiss()
                    var frag = UnApprovedEntriesScreen(currentSelectedLedgerUID)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_main,frag)
                        .commit()
                }else{
                    if (entry.requestMode==2){
                        Toast.makeText(context, "Already Requested For Deletion. Please Check Un-Approved Entries", Toast.LENGTH_SHORT).show()
                  rv.adapter!!.notifyDataSetChanged()
                    }
                }




            }
            .setNegativeButton("No"){dialogInterface,it->
                //cancel it
            dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
    return dialog
    }

    fun getEditDialougeBox(pos: Int, rv: RecyclerView): AlertDialog.Builder{

        var dialog= AlertDialog.Builder(this.context)

        dialog.setTitle("Edit Ledger Entry")
            .setMessage("Are you sure to Edit entry!")
            .setCancelable(true)
            .setPositiveButton("Yes Delete it"){dialogInterface,it->
                //delete entry

                dialogInterface.dismiss()
                rv.adapter!!.notifyDataSetChanged()
            }
            .setNegativeButton("No"){dialogInterface,it->
                //cancel it
                dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
        return dialog
    }

}