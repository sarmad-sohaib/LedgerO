package com.ledgero.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.UtillClasses.CalculatorUtill
import kotlinx.android.synthetic.main.calculator_layout.view.*
import kotlinx.android.synthetic.main.fragment_add_new_entry_detail.view.*
import kotlinx.android.synthetic.main.fragment_money.view.*
import net.objecthunter.exp4j.Expression


class AddNewEntryDetail : Fragment() {


    lateinit var amountTextTV:EditText
    lateinit var totalAmount:EditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {


       var view=inflater.inflate(R.layout.fragment_add_new_entry_detail, container, false)
        var entryMode=-1  // mode tell if user pressed Got or Gave

        amountTextTV= view.tv_amount_add_new_entry
        totalAmount= view.tv_Totalamount_add_new_entry

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


        setCalculatorBtnListeners(view)


        view.bt_add_new_entry.setOnClickListener(){




            if (!totalAmount.text.isNullOrBlank() && !totalAmount.text.toString().equals("Err")){

                if (!view.tv_description_add_new_entry.text.isNullOrBlank()){

                    Toast.makeText(context, "Adding New Entry To Ledger", Toast.LENGTH_SHORT).show()

                    val amount: Float = java.lang.Float.valueOf(totalAmount.text.toString())

                    var des= view.tv_description_add_new_entry.text.toString()

                    var title= if (des.length>16)des.subSequence(0,15).toString() else des.toString()

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




        amountTextTV.setInputType(InputType.TYPE_NULL) // disable soft input



        amountTextTV.tv_amount_add_new_entry.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                //this will close the soft keyboard if its open
                // hide virtual keyboard

                hideKeyboard()
                if (view.calculator_layout_add_new_entry_include.visibility==View.GONE){
                    view.calculator_layout_add_new_entry_include.visibility=View.VISIBLE

                }

                return false
        }})





        view.tv_description_add_new_entry.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                view.calculator_layout_add_new_entry_include.visibility=View.GONE
return false

            }

        })


        amountTextTV.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
             ;
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
              ;
            }

            override fun afterTextChanged(p0: Editable?) {
               if (view.tv_amount_add_new_entry.text.isNullOrEmpty()){
                   view.entryInfoScrollView_add_new_entry_screen.visibility=View.GONE
               }else{
                   view.entryInfoScrollView_add_new_entry_screen.visibility=View.VISIBLE

               }
            }

        })



        return view
    }

    private fun setCalculatorBtnListeners(view: View) {


        CalculatorUtill.setClickListenersOnButtons(view,this)

    }



    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStop() {

        super.onStop()

    }


    /*Function to calculate the expressions using expression builder library*/

    fun evaluateExpression(string: String, clear: Boolean) {
        if(clear) {

            amountTextTV.append(string)
        } else {
            amountTextTV.append(totalAmount.text)
            amountTextTV.append(string)
            totalAmount.setText("")
        }
    }
}