package com.ledgero


import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ledgero.fragments.*
import com.ledgero.more.ui.MoreFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var curruntFragment: Fragment
    private lateinit var main_frag_holder: FrameLayout


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

        main_frag_holder = findViewById(R.id.fl_fragment_container_main)
//         Restricting darkMode in this activity
//        TODO: change this line when dark mode needed in the activity
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //disabling dark mode from this activity

        val menuFragment = intent.getStringExtra("fragmentName")

        //notification tap back
        if (menuFragment != null) {
            val mLedgerUID = intent.getStringExtra("ledgerUID")

            Log.d("TapBack", "onCreate:  $mLedgerUID")
            // Use the Kotlin extension in the fragment-ktx artifact
            supportFragmentManager.setFragmentResult("fragmentName", bundleOf("ledgerUID" to mLedgerUID))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_fragment_container_main, HomeFragment()).commit()
        }else
        {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment_container_main, HomeFragment()).commit()
    }
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
            main_frag_holder.removeAllViews()
            supportFragmentManager.popBackStack()

        }else{

            main_frag_holder.removeAllViews()
            super.onBackPressed()

        }
    }
}