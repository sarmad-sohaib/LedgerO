package com.ledgero.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.ledgero.R
import com.ledgero.models.PagerAdapter

class HomeFragment : Fragment() {

    private lateinit var tabs: TabLayout
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view:View = inflater.inflate(R.layout.fragment_home, container, false)

        tabs = view.findViewById(R.id.tabs_home)
        val viewPager = view.findViewById<ViewPager>(R.id.vp_home)

        pagerAdapter = PagerAdapter(childFragmentManager)

        pagerAdapter.addFragmant(LedgersFragment(), "Ledgers")
        pagerAdapter.addFragmant(GroupLedgersFragment(), "Group Ledgers")

        tabs.setupWithViewPager(viewPager)
        viewPager.adapter = pagerAdapter

        return view
    }
}