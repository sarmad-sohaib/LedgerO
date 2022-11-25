package com.ledgero.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.databinding.FragmentGroupLedgersBinding


private const val TAG="GroupLedgerFragment"
class GroupLedgersFragment : Fragment() {

    private lateinit var _binding: FragmentGroupLedgersBinding
    private val binding: FragmentGroupLedgersBinding get()= _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding= FragmentGroupLedgersBinding.inflate(inflater,container,false)

        val cashRegisterButton = binding.btCashRegisterGroupLedgersFrag
        cashRegisterButton.setOnClickListener{
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }



        binding.btAddNewGroup.setOnClickListener {  }

        return binding.root
    }
}