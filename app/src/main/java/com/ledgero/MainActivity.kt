package com.ledgero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ledgero.fragments.HomeFragment
import com.ledgero.fragments.MoneyFragment
import com.ledgero.fragments.MoreFragment

class MainActivity : AppCompatActivity() {

    private lateinit var curruntFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        TODO: change this line when dark mode needed in the activity
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //disabling dark mode from this activity

        supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_container_main, HomeFragment()).commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


        val navLisner = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_home -> {

//                    curruntFragment = HomeFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_container_main, HomeFragment()).commit()
                    true
                }
                R.id.menu_money -> {
                    // Respond to navigation item 2 click
//                    curruntFragment = MoneyFragment()

                    supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_container_main, MoneyFragment()).commit()
                    true
                }

                R.id.menu_more -> {

//                    curruntFragment = MoreFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_container_main, MoreFragment()).commit()
                    true
                }
                else -> {false}
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener (navLisner)

    }
}