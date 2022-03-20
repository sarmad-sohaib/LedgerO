package com.ledgero.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ContextUtils.getActivity
import com.ledgero.R
import kotlinx.android.synthetic.main.fragment_ledgers.*
class LedgersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view =  inflater.inflate(R.layout.fragment_ledgers, container, false)
        var dialog = CustomDialogFragment()
        var bt = view.findViewById<MaterialButton>(R.id.bt_add_new_ledger)

        bt.setOnClickListener(){
            dialog.show(childFragmentManager, "CustomDialog")
        }
        return view
    }
}