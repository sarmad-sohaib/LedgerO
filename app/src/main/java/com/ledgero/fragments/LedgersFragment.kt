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
import com.ledgero.DataClasses.User
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.adapters.RecyclerViewAdapter
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.databinding.FragmentLedgersBinding
import com.ledgero.model.DatabaseUtill
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG


private const val  TAG= "LedgerFragment"
class LedgersFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var _binding: FragmentLedgersBinding
    private val binding:FragmentLedgersBinding get() = _binding!!

    companion object{
        var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLedgersBinding.inflate(inflater,container,false)

        DatabaseUtill().UserLedgerListner()
        val bt = binding.btAddNewLedger
        val rv = binding.rvLedgers

        Toast.makeText(context, "Username:  ${User.userName}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onCreateView: User Name ${User.userName}")
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager

        val userLedgers= User.getUserSingleLedgers()

        adapter = RecyclerViewAdapter(requireContext(),userLedgers)
        rv.adapter = adapter
        val btCashRegister= binding.btCashRegisterGroupLedgersFrag
        btCashRegister.setOnClickListener {
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }
        bt.setOnClickListener {

            Toast.makeText(context, "Add New Clicked", Toast.LENGTH_SHORT).show()
            val dialog= CustomDialogFragment(rv.adapter)
            dialog.show(childFragmentManager,"customDialog")

            Toast.makeText(context, "Show Dialog Called", Toast.LENGTH_SHORT).show()


        }

       // Toast.makeText(context, "Going to start Fragment Listening", Toast.LENGTH_SHORT).show()
        setFragmentResultListener("fragmentName") { _, bundle ->
         try {
             val passedLedgerUID = bundle.getString("ledgerUID")
             Log.d("TapBack", "onCreateView: $passedLedgerUID ")
             if (passedLedgerUID !=  null){

                 Toast.makeText(context, "Ledger UID : $passedLedgerUID", Toast.LENGTH_SHORT).show()
                 val frag=IndividualLedgerScreen(passedLedgerUID)
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

        var totalAmountAndFlag= DatabaseUtill().singleLedgersTotalMetaData()

        val amount= totalAmountAndFlag.first
        val flag= totalAmountAndFlag.second

        if (amount>0){
            if (flag == GAVE_ENTRY_FLAG) binding.tvGetMoneyFrag.text = amount.toString()
            if (flag == GET_ENTRY_FLAG) binding.tvGiveMoneyFrag.text = amount.toString()
        }

        return binding.root
    }


    override fun onDestroy() {
        DatabaseUtill().RemoveUserLedgerListner()
        super.onDestroy()
    }
}