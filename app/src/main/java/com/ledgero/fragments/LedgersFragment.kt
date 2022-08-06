package com.ledgero.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.adapters.RecyclerViewAdapter

class LedgersFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null
    private var TAG= "LedgerFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_ledgers, container, false)
        var bt = view.findViewById<MaterialButton>(R.id.bt_add_new_ledger)
        var rv = view.findViewById<RecyclerView>(R.id.rv_ledgers)

        Toast.makeText(context, "${User.userName}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onCreateView: User Name ${User.userName}")
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager

        adapter = RecyclerViewAdapter(requireContext(),User.user_single_Ledgers)
        rv.adapter = adapter

        var bt_cash_register= view.findViewById<MaterialButton>(R.id.bt_cash_register_money_frag)
        bt_cash_register.setOnClickListener(){
            //this is for testing...you can remove it
            rv.adapter!!.notifyItemInserted(User.user_single_Ledgers!!.size)
        }
        bt.setOnClickListener(){

            Toast.makeText(context, "Add New Clicked", Toast.LENGTH_SHORT).show()
            var dialog= CustomDialogFragment(rv.adapter)
            dialog.show(childFragmentManager,"customDialog")
            rv.adapter!!.notifyDataSetChanged()
            Toast.makeText(context, "Show Dialog Called", Toast.LENGTH_SHORT).show()


        }

        return view
    }
}