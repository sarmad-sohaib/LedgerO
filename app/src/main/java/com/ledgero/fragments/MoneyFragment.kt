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
import com.ledgero.reminders.RemindersMainActivity

class MoneyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_money, container, false)

        val buttonCashRegister = view.findViewById<Button>(R.id.bt_cash_register_group_ledgers_frag)
        val buttonReminders = view.findViewById<View >(R.id.button_reminders)

        buttonCashRegister.setOnClickListener() {
            startActivity(Intent(requireContext(), CashRegisterMainActivity::class.java))
        }

        buttonReminders.setOnClickListener {
            startActivity(Intent(requireContext(), RemindersMainActivity::class.java))
        }
        // Inflate the layout for this fragment
        return view
    }

}