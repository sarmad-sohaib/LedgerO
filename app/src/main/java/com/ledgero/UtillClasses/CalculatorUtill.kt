package com.ledgero.UtillClasses

import android.view.View
import android.widget.Toast
import com.ledgero.fragments.AddNewEntryDetail
import kotlinx.android.synthetic.main.calculator_layout.view.*
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.ArithmeticException

class CalculatorUtill {



    companion object{
        fun setClickListenersOnButtons(view: View, addNewEntryDetail: AddNewEntryDetail) {

            /*Number Buttons*/

            view.btnDoubleZero.setOnClickListener {
                addNewEntryDetail.evaluateExpression("00", clear = true)
                performEqualOperation(addNewEntryDetail)
            }
            view.btnOne.setOnClickListener {
                addNewEntryDetail.evaluateExpression("1", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnTwo.setOnClickListener {
                addNewEntryDetail.evaluateExpression("2", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnThree.setOnClickListener {
                addNewEntryDetail. evaluateExpression("3", clear = true)
                performEqualOperation(addNewEntryDetail)
            }
            view.btnFour.setOnClickListener {
                addNewEntryDetail.  evaluateExpression("4", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnFive.setOnClickListener {
                addNewEntryDetail. evaluateExpression("5", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnSix.setOnClickListener {
                addNewEntryDetail.evaluateExpression("6", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnSeven.setOnClickListener {
                addNewEntryDetail.evaluateExpression("7", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnEight.setOnClickListener {
                addNewEntryDetail.evaluateExpression("8", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnNine.setOnClickListener {
                addNewEntryDetail.evaluateExpression("9", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnZero.setOnClickListener {
                addNewEntryDetail.evaluateExpression("0", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            /*Operators*/

            view.btnPercent.setOnClickListener {
                addNewEntryDetail.evaluateExpression("/100", clear = true)
                performEqualOperation(addNewEntryDetail)
            }
            view.btnPlus.setOnClickListener {
                addNewEntryDetail.evaluateExpression("+", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnMinus.setOnClickListener {
                addNewEntryDetail.evaluateExpression("-", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnMul.setOnClickListener {
                addNewEntryDetail.evaluateExpression("*", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnDivide.setOnClickListener {
                addNewEntryDetail.evaluateExpression("/", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnDot.setOnClickListener {
                addNewEntryDetail. evaluateExpression(".", clear = true)
                performEqualOperation(addNewEntryDetail)
            }

            view.btnClear.setOnClickListener {
                addNewEntryDetail.amountTextTV.setText("")
                addNewEntryDetail.totalAmount.setText("")
            }

            view.btnEquals.setOnClickListener {
         performEqualOperation(addNewEntryDetail)
                addNewEntryDetail.amountTextTV.setText(addNewEntryDetail.totalAmount.text)

            }


            view.btnBack.setOnClickListener {
                var text =  addNewEntryDetail.amountTextTV.text.toString()

                if(text.isNotEmpty()) {
                    text= text.dropLast(1)
                    addNewEntryDetail.amountTextTV.setText(text)
                    if (text.isNotEmpty()){
                        performEqualOperation(addNewEntryDetail)
                    }else{

                        addNewEntryDetail.totalAmount.setText( "")
                    }
                }

            }

        }


        private  fun performEqualOperation(addNewEntryDetail:AddNewEntryDetail){
            val text = addNewEntryDetail.amountTextTV.text.toString()

            if (!isExpressionCorrect(text)){

                addNewEntryDetail.totalAmount.setText("Err")
             return
            }

            val expression = ExpressionBuilder(text).build()

            try {
                val result = expression.evaluate()
                val longResult = result.toLong()
                if (result == longResult.toDouble()) {
                    addNewEntryDetail.totalAmount.setText(longResult.toString())
                } else {
                    addNewEntryDetail.totalAmount.setText(result.toString())
                }
            }catch (e:ArithmeticException){
                Toast.makeText(addNewEntryDetail.context, "Wrong Expression! ${e.message}", Toast.LENGTH_LONG).show()
            }

        }

        private fun isExpressionCorrect(text: String): Boolean {

            if (text.isEmpty()){
                return false
            }

            var flag= !(text.last().equals('+') || text.last().equals('-') || text.last().equals('/') || text.last().equals('*') || text.last().equals('.'))

            return flag
        }
    }

    }
