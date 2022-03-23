package com.ledgero.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ContextUtils.getActivity
import com.ledgero.R
import com.ledgero.adapters.RecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_ledgers.*
class LedgersFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view =  inflater.inflate(R.layout.fragment_ledgers, container, false)
        var dialog = CustomDialogFragment()
        var bt = view.findViewById<MaterialButton>(R.id.bt_add_new_ledger)
        var rv = view.findViewById<RecyclerView>(R.id.rv_ledgers)


        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager

        adapter = RecyclerViewAdapter()
        rv.adapter = adapter


        bt.setOnClickListener(){
            dialog.show(childFragmentManager, "CustomDialog")
        }
        return view
    }
}