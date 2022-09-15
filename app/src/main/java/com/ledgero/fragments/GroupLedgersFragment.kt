package com.ledgero.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ledgero.R
import com.ledgero.cashregister.CashRegisterMainActivity


class GroupLedgersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_group_ledgers, container, false)

        val cashRegisterButton = view.findViewById<Button>(R.id.bt_cash_register_group_ledgers_frag)

        cashRegisterButton.setOnClickListener{
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }

        return view
    }
}