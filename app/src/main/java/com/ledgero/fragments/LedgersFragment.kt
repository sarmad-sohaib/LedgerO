package com.ledgero.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ledgero.R
import com.ledgero.adapters.RecyclerViewAdapter

class LedgersFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_ledgers, container, false)
        var dialog = CustomDialogFragment()
        var bt = view.findViewById<MaterialButton>(R.id.bt_add_new_ledger)
        var rv = view.findViewById<RecyclerView>(R.id.rv_ledgers)

        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager

        adapter = RecyclerViewAdapter()
        rv.adapter = adapter


        bt.setOnClickListener {
            dialog.show(childFragmentManager, "CustomDialog")
        }
        return view
    }
}