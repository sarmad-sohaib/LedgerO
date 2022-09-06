package com.ledgero.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.model.DatabaseUtill
import kotlinx.android.synthetic.main.fragment_add_new_entry_detail.view.*

class AddNewEntryDetail : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


       var view=inflater.inflate(R.layout.fragment_add_new_entry_detail, container, false)
        var entryMode=-1  // mode tell if user pressed Got or Gave

        setFragmentResultListener("addEntryBtn"){addEntryBtn,bundle ->

            entryMode=bundle.getInt("mode")

            if (entryMode==1)//means user pressed GOT
         {
             view.add_new_entry_title.setTextColor(Color.RED)

         }
            if (entryMode==0){

                view.add_new_entry_title.setTextColor(Color.GREEN)

            }

        }



        view.bt_add_new_entry.setOnClickListener(){



            if (!view.tv_amount_add_new_entry.text.isNullOrBlank()){

                if (!view.tv_description_add_new_entry.text.isNullOrBlank()){

                    Toast.makeText(context, "Adding New Entry To Ledger", Toast.LENGTH_SHORT).show()

                    val amount: Float = java.lang.Float.valueOf(view.tv_amount_add_new_entry.text.toString())

                    var des= view.tv_description_add_new_entry.text.toString()
                    var title= "title "

                    var flag = if (entryMode==1) true else false

                    var entry = Entries(amount,flag,des,title,0,false,User.userID,"",1)


                    IndividualLedgerScreen.instanceObject!!.viewModel.addNewEntry(entry)

                   var frag = UnApprovedEntriesScreen(IndividualLedgerScreen.instanceObject!!.currentSelectedLedgerUID.toString())


                    parentFragmentManager.popBackStack()
                    parentFragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fl_fragment_container_main,frag)
                        .commit()



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