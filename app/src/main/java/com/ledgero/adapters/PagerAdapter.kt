package com.ledgero.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager2.adapter.FragmentStateAdapter

// This is adapter class for the viewpager for the homeFragment

class PagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {

    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitle = ArrayList<String>()


//    override fun getCount(): Int = fragmentList.size
//
//    override fun getItem(position: Int): Fragment = fragmentList[position]
//
//    override fun getPageTitle(position: Int): CharSequence = fragmentTitle[position]

    fun addFragmant(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitle.add(title)
    }

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]


}