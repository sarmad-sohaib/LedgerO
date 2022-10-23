package com.ledgero.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// This is adapter class for the viewpager for the homeFragment

// Don't use deprecated classes
// Use better name then sFM
class PagerAdapter(sFM: FragmentManager) :
    FragmentPagerAdapter(sFM, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    // Better make a single array to prevent mistakes when supplying titles and fragments separately
    // private val pagerFragments = ArrayList<Pair<String, Fragment>>()
    //
    // Even better approach would be to define the names of fragment in themselves in companion objects
    // and access them statically. In this case, as we don't know the names of fragments in this class
    // we can define a base class for these fragments, e.g., PagerFragment and in that have a
    // property "title" and a method geFragmentTitle
    // private val fragmentList = ArrayList<PagerFragment>()
    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitle = ArrayList<String>()

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getPageTitle(position: Int): CharSequence? = fragmentTitle[position]

    // Typo
    fun addFragmant(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitle.add(title)
    }
}