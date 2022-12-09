package com.ledgero.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ledgero.R
import com.ledgero.adapters.PagerAdapter
import com.ledgero.groupLedger.GroupLedgersFragment

class HomeFragment : Fragment() {

    private lateinit var tabs: TabLayout
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        val tabTitles = listOf<String>("Ledgers", "Group Ledgers")

        //Accessing views using id
        tabs = view.findViewById(R.id.tabs_home)
        viewPager = view.findViewById(R.id.vp_home)

        pagerAdapter = PagerAdapter(requireActivity())

        //for notificaiton tap back
        setFragmentResultListener("fragmentName") { fragmentName, bundle ->
            val passedLedgerUID = bundle.getString("ledgerUID")
            Log.d("TapBack", "onCreateView: $passedLedgerUID")
            if (passedLedgerUID != null) {


                childFragmentManager.setFragmentResult(
                    "fragmentName",
                    bundleOf("ledgerUID" to passedLedgerUID)
                )


            }
        }


        pagerAdapter.addFragmant(LedgersFragment(), "Ledgers")
        pagerAdapter.addFragmant(GroupLedgersFragment(), "Group Ledgers")

//        tabs.setupWithViewPager(viewPager)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        return view
    }
}