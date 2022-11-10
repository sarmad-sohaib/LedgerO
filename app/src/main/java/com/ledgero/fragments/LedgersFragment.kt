package com.ledgero.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ledgero.DataClasses.User
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.adapters.RecyclerViewAdapter
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.model.DatabaseUtill

class LedgersFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null

    companion object{
        var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null


    }
    private var TAG= "LedgerFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        DatabaseUtill().UserLedgerListner()
        var view = inflater.inflate(R.layout.fragment_ledgers, container, false)
        var bt = view.findViewById<MaterialButton>(R.id.bt_add_new_ledger)
        var rv = view.findViewById<RecyclerView>(R.id.rv_ledgers)

        Toast.makeText(context, "${User.userName}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onCreateView: User Name ${User.userName}")
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager

        adapter = RecyclerViewAdapter(requireContext(),User.getUserSingleLedgers())
        rv.adapter = adapter
        var bt_cash_register= view.findViewById<MaterialButton>(R.id.bt_cash_register_group_ledgers_frag)

        bt_cash_register.setOnClickListener {
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }
        bt.setOnClickListener {

            Toast.makeText(context, "Add New Clicked", Toast.LENGTH_SHORT).show()
            var dialog= CustomDialogFragment(rv.adapter)
            dialog.show(childFragmentManager,"customDialog")

            Toast.makeText(context, "Show Dialog Called", Toast.LENGTH_SHORT).show()


        }

        Toast.makeText(context, "Going to start Fragment Listening", Toast.LENGTH_SHORT).show()
        setFragmentResultListener("fragmentName") { fragmentName, bundle ->
         try {
             val passedLedgerUID = bundle.getString("ledgerUID")
             Log.d("TapBack", "onCreateView: $passedLedgerUID ")
             if (passedLedgerUID !=  null){

                 Toast.makeText(context, "Ledger UID : $passedLedgerUID", Toast.LENGTH_SHORT).show()
                 var frag=IndividualLedgerScreen(passedLedgerUID)
                 Log.d("TapBack", "onCreateView: $passedLedgerUID ")

                 MainActivity.getMainActivityInstance().supportFragmentManager
                     .beginTransaction()
                     .replace(R.id.fl_fragment_container_main, frag)
                     .addToBackStack(null)
                     .commit()

             } 
         }catch (e: Exception){
             Log.d(TAG, "onCreateView: ${e.message}")
         }
           

        }


        return view
    }


    override fun onDestroy() {
        DatabaseUtill().RemoveUserLedgerListner()
        super.onDestroy()
    }
}