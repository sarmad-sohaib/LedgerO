package com.ledgero.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.adapters.PagerAdapter
import com.ledgero.groupLedger.GroupLedgersFragment
import com.ledgero.model.DatabaseUtill

class HomeFragment : Fragment() {

    private lateinit var tabs: TabLayout
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        //Accessing views using id
        tabs = view.findViewById(R.id.tabs_home)
        viewPager = view.findViewById(R.id.vp_home)

        pagerAdapter = PagerAdapter(childFragmentManager)

        //for notificaiton tap back
        setFragmentResultListener("fragmentName") { fragmentName, bundle ->
            val passedLedgerUID = bundle.getString("ledgerUID")
            Log.d("TapBack", "onCreateView: $passedLedgerUID")
           if (passedLedgerUID !=  null){


                childFragmentManager.setFragmentResult("fragmentName", bundleOf("ledgerUID" to passedLedgerUID))


            }}


        pagerAdapter.addFragmant(LedgersFragment(), "Ledgers")
        pagerAdapter.addFragmant(GroupLedgersFragment(), "Group Ledgers")

        tabs.setupWithViewPager(viewPager)
        viewPager.adapter = pagerAdapter

        return view
    }
}