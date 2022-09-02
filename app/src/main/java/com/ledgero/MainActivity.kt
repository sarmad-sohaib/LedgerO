package com.ledgero


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ledgero.fragments.HomeFragment
import com.ledgero.fragments.MoneyFragment
import com.ledgero.fragments.MoreFragment


class MainActivity : AppCompatActivity() {

    private lateinit var curruntFragment: Fragment


    companion object {
        lateinit var mainActivity: MainActivity


        fun getMainActivityInstance(): MainActivity {
            return mainActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivity = this

//         Restricting darkMode in this activity
//        TODO: change this line when dark mode needed in the activity
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //disabling dark mode from this activity

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment_container_main, HomeFragment()).commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val navLisner = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {

//                    curruntFragment = HomeFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_main, HomeFragment()).commit()
                    true
                }
                R.id.menu_money -> {
                    // Respond to navigation item 2 click
//                    curruntFragment = MoneyFragment()


                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_main, MoneyFragment()).commit()
                    true
                }

                R.id.menu_more -> {

//                    curruntFragment = MoreFragment()

                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_main, MoreFragment()).commit()
                    true
                }
                else -> {
                    false
                }
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(navLisner)


    }


    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount>0){
            supportFragmentManager.popBackStack()

        }else{

            super.onBackPressed()

        }
    }
}