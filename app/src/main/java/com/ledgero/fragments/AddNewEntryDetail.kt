package com.ledgero.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.MainActivity
import com.ledgero.R
import kotlinx.android.synthetic.main.fragment_add_new_entry_detail.view.*

class AddNewEntryDetail : Fragment() {


    lateinit var currentLedger: SingleLedgers
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


       var view=inflater.inflate(R.layout.fragment_add_new_entry_detail, container, false)
        var entryMode=-1  // mode tell if user pressed Got or Gave

        setFragmentResultListener("addEntryBtn"){addEntryBtn,bundle ->

            entryMode=bundle.getInt("mode")
            currentLedger=bundle.get("ledger") as SingleLedgers
            if (entryMode==1)//means user pressed GOT
         {
             view.add_new_entry_title.setTextColor(Color.RED)

         }
            if (entryMode==0){

                view.add_new_entry_title.setTextColor(Color.GREEN)

            }

        }



        view.bt_add_new_entry.setOnClickListener(){


            Toast.makeText(context, currentLedger.friend_userName, Toast.LENGTH_SHORT).show()

            if (!view.tv_amount_add_new_entry.text.isNullOrBlank()){

                if (!view.tv_description_add_new_entry.text.isNullOrBlank()){

                    Toast.makeText(context, "Adding New Entry To Ledger", Toast.LENGTH_SHORT).show()

                    val amount: Float = java.lang.Float.valueOf(view.tv_amount_add_new_entry.text.toString())

                    var des= view.tv_description_add_new_entry.text.toString()
                    var title= "title "
                    if(!currentLedger.entries.isNullOrEmpty()){
                         title= title+currentLedger.entries?.size!!+1
                    }else{
                        title= title+"1"
                    }
                    var flag = if (entryMode==1) true else false
                    var entry = Entries(amount,flag,des,title,0)

                    currentLedger.addEntry(entry)

                    var frag= IndividualLedgerScreen()
                    frag.data(currentLedger.ledgerUID.toString())
                    MainActivity.getMainActivityInstance().setFragment(frag,false,"individScreen")


                }else{
                    Toast.makeText(context, "No Description Added!!", Toast.LENGTH_SHORT).show()

                }
            }else{
                Toast.makeText(context, "Please Enter Amount!!", Toast.LENGTH_SHORT).show()

            }
        }





        return view
    }

    override fun onStop() {

        super.onStop()

    }
}