package com.ledgero.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// This is adapter class for the viewpager for the homeFragment

class PagerAdapter(sFM: FragmentManager): FragmentPagerAdapter(sFM, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitle = ArrayList<String>()


    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getPageTitle(position: Int): CharSequence? = fragmentTitle[position]

    fun addFragmant(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitle.add(title)
    }



}