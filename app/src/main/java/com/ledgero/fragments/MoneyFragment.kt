package com.ledgero.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.other.Constants
import com.ledgero.reminders.RemindersMainActivity

class MoneyFragment : Fragment() {

    private lateinit var  inText: TextView
    private lateinit var  outText: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_money, container, false)

        val buttonCashRegister = view.findViewById<Button>(R.id.bt_cash_register_group_ledgers_frag)
        val buttonReminders = view.findViewById<View >(R.id.button_reminders)
        outText= view.findViewById(R.id.tv_get_money_frag_money)
        inText= view.findViewById(R.id.tv_give_money_frag_money)
        buttonCashRegister.setOnClickListener {
            startActivity(Intent(requireContext(), CashRegisterMainActivity::class.java))
        }

        buttonReminders.setOnClickListener {
            startActivity(Intent(requireContext(), RemindersMainActivity::class.java))
        }
        // Inflate the layout for this fragment
        return view
    }


    override fun onResume() {
        super.onResume()
        calculateTotalAmount(User.getUserSingleLedgers())

    }
    private fun calculateTotalAmount(ledgers: ArrayList<SingleLedgers>?){

        if (ledgers?.size!!<=0){
           inText.setText("Rs. 00")
            outText.setText("Rs. 00")

            return
        }


        var totalGaveAmount= 0f
        var  totalGetAmount = 0f


        ledgers.forEach{



            if (it.ledgerCreatedByUID == User.userID){

                if (it.give_take_flag == Constants.GAVE_ENTRY_FLAG){

                    totalGaveAmount += it.total_amount!!
                }
                if (it.give_take_flag == Constants.GET_ENTRY_FLAG){
                    totalGetAmount+= it.total_amount!!
                }

            }

            if (it.ledgerCreatedByUID != User.userID){

                if (it.give_take_flag == Constants.GAVE_ENTRY_FLAG){
                    totalGetAmount+= it.total_amount!!

                }
                if (it.give_take_flag == Constants.GET_ENTRY_FLAG){
                    totalGaveAmount += it.total_amount!!
                }
            }
        }

        var flag= false
        var totalAmount =0f
        if (totalGaveAmount>=totalGetAmount){
            flag= Constants.GAVE_ENTRY_FLAG
            totalAmount= totalGaveAmount- totalGetAmount
        }
        if (totalGetAmount>totalGaveAmount){
            flag= Constants.GET_ENTRY_FLAG
            totalAmount = totalGetAmount-totalGaveAmount
        }



        if (flag == Constants.GAVE_ENTRY_FLAG){
            inText.setText("Rs. 00")
            outText.setText("Rs. $totalAmount")

        }
        if (flag == Constants.GET_ENTRY_FLAG){
            inText.setText("Rs. $totalAmount")
            outText.setText("Rs. 00")
        }

        LedgersFragment.adapter?.notifyDataSetChanged()
    }

}