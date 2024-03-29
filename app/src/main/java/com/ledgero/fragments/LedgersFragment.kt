package com.ledgero.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.ViewReportActivity
import com.ledgero.adapters.RecyclerViewAdapter
import com.ledgero.adapters.ViewReportAdapter
import com.ledgero.cashregister.CashRegisterMainActivity
import com.ledgero.databinding.FragmentLedgersBinding
import com.ledgero.model.DatabaseUtill
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val  TAG= "LedgerFragment"
class LedgersFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var _binding: FragmentLedgersBinding
    private val binding:FragmentLedgersBinding get() = _binding

    companion object{
        var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLedgersBinding.inflate(inflater,container,false)

        DatabaseUtill().UserLedgerListner()
        val bt = binding.btAddNewLedger
        val rv = binding.rvLedgers

        Toast.makeText(context, "Username:  ${User.userName}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onCreateView: User Name ${User.userName}")
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager

        val userLedgers= User.getUserSingleLedgers()

        adapter = RecyclerViewAdapter(requireContext(),userLedgers)
        rv.adapter = adapter
        getTouchHelper(rv).attachToRecyclerView(rv)


            lifecycleScope.launch{
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                    withContext(Dispatchers.Main){
                        binding.progressBarLedgerFragment.visibility= View.VISIBLE

                        delay(1000)
                        binding.progressBarLedgerFragment.visibility = View.GONE
                        if (User.getUserSingleLedgers() != null){
                            Log.d(TAG, "onCreateView: fetching ledgers MetaData")
                            Log.d(TAG, "onCreateView:  before loopin : ${User.getUserSingleLedgers()!!.size}")


                            for (i in 0 until User.getUserSingleLedgers()!!.size){
                                Log.d(TAG, "onCreateView: loopin : ${User.getUserSingleLedgers()!!.size}")

                                FirebaseDatabase.getInstance().reference.child("ledgerInfo")
                                    .child(User.getUserSingleLedgers()!![i].ledgerUID!!).get().addOnCompleteListener {
                                        if(it.isSuccessful){
                                            if (it.result.exists()){
                                                val giveTakeFlag = it.result.child("give_take_flag").value as Boolean

                                                val timeStamp = it.result.child("ledger_Created_timeStamp").value as Long
                                                val totalAmount = it.result.child("total_amount").value.toString().toFloat()

                                                User.getUserSingleLedgers()!![i].give_take_flag=giveTakeFlag
                                                User.getUserSingleLedgers()!![i].ledger_Created_timeStamp = timeStamp
                                                User.getUserSingleLedgers()!![i].total_amount = totalAmount

                                                if (i == User.getUserSingleLedgers()!!.size-1)
                                                    rv.swapAdapter(RecyclerViewAdapter(requireContext(),userLedgers),false)
                                                calculateTotalAmount(User.getUserSingleLedgers()!!)
                                            }
                                        }
                                    }

                            }

                        }
                    }
                }
            }



        val btCashRegister= binding.btCashRegisterGroupLedgersFrag
        btCashRegister.setOnClickListener {
            startActivity(Intent(context, CashRegisterMainActivity::class.java))
        }
        bt.setOnClickListener {

            Toast.makeText(context, "Add New Clicked", Toast.LENGTH_SHORT).show()
            val dialog= CustomDialogFragment(rv.adapter)
            dialog.show(childFragmentManager,"customDialog")

            Toast.makeText(context, "Show Dialog Called", Toast.LENGTH_SHORT).show()


        }

       // Toast.makeText(context, "Going to start Fragment Listening", Toast.LENGTH_SHORT).show()
        setFragmentResultListener("fragmentName") { _, bundle ->
         try {
             val passedLedgerUID = bundle.getString("ledgerUID")
             Log.d("TapBack", "onCreateView: $passedLedgerUID ")
             if (passedLedgerUID !=  null){

                 Toast.makeText(context, "Ledger UID : $passedLedgerUID", Toast.LENGTH_SHORT).show()
                 val frag=IndividualLedgerScreen(passedLedgerUID)
                 Log.d("TapBack", "onCreateView: $passedLedgerUID ")

                 MainActivity.getMainActivityInstance().supportFragmentManager
                     .beginTransaction()
                     .replace(R.id.fl_fragment_container_main, frag)
                     .addToBackStack(null)
                     .commit()

             } 
         }catch (e: Exception){
             Log.d(TAG, "onCreateView: ${e.message}")
         }
           

        }

        val totalAmountAndFlag= DatabaseUtill().singleLedgersTotalMetaData()

        val amount= totalAmountAndFlag.first
        val flag= totalAmountAndFlag.second

        if (amount>0){
            if (flag == GAVE_ENTRY_FLAG) binding.tvGetMoneyFrag.text = amount.toString()
            if (flag == GET_ENTRY_FLAG) binding.tvGiveMoneyFrag.text = amount.toString()
        }


        binding.tvPaymentHistoryMoneyFrag.setOnClickListener {
            val m = Intent(requireContext(),ViewReportActivity::class.java)
            startActivity(m)
        }

        return binding.root
    }


    //this will enable left/right swipes on RV
    private fun getTouchHelper(rv: RecyclerView): ItemTouchHelper {

        val callBack: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.
        SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT) {

                    getDeleteDialogueBox(position, rv).show()
                    rv.adapter!!.notifyDataSetChanged()


                }
                if (direction == ItemTouchHelper.RIGHT) {


                    Toast.makeText(context, "Swipe Left to delete the LEdger", Toast.LENGTH_SHORT)
                        .show()
                    rv.adapter!!.notifyDataSetChanged()

                }

            }


        }


        return ItemTouchHelper(callBack)

    }


    @SuppressLint("NotifyDataSetChanged")
    fun getDeleteDialogueBox(pos: Int, rv: RecyclerView): AlertDialog.Builder{

        val dialog= AlertDialog.Builder(this.context)

        dialog.setTitle("Deleting Ledger ")
            .setMessage("Are you sure to delete this ledger!")
            .setCancelable(true)
            .setPositiveButton("Yes Delete it"){ _, _ ->
                //delete ledger

                val ledgerUID=User.getUserSingleLedgers()
                    ?.get(pos)!!.ledgerUID.toString()

                FirebaseDatabase.getInstance().reference.child("deleteLedgerRequests").child(ledgerUID).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            if (it.result.exists()){
                                Toast.makeText(context, "Already Requested for delete...Waiting for approval", Toast.LENGTH_SHORT).show()

                            }else{
                                FirebaseDatabase.getInstance().reference.child("deleteLedgerRequests").child(ledgerUID)
                                    .setValue(mapOf("deleteRequestBy" to User.userID!! , "requesterName" to User.userName!!))

                                Toast.makeText(context, "Request To Delete Ledger is Sent..Waiting For Approval", Toast.LENGTH_SHORT).show()

                            }
                        }else{
                            FirebaseDatabase.getInstance().reference.child("deleteLedgerRequests").child(ledgerUID)
                                .setValue(mapOf("deleteRequestBy" to User.userID!!))

                            Toast.makeText(context, "Request To Delete Ledger is Sent..Waiting For Approval", Toast.LENGTH_SHORT).show()

                        }
                    }


            }
            .setNegativeButton("No"){ dialogInterface, _ ->
                //cancel it
                dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
        return dialog
    }


    override fun onDestroy() {
        DatabaseUtill().RemoveUserLedgerListner()
        super.onDestroy()
    }

    private fun calculateTotalAmount(ledgers: ArrayList<SingleLedgers>){
       Log.d(TAG, "calculation:  ${ledgers.size}")

        if (ledgers.size<=0){
            binding.tvGiveMoneyFrag.setText("Rs. 00")
            binding.tvGetMoneyFrag.setText("Rs. 00")

            return
        }


        var totalGaveAmount= 0f
         var  totalGetAmount = 0f


                ledgers.forEach{



                    if (it.ledgerCreatedByUID == User.userID){

                        if (it.give_take_flag == GAVE_ENTRY_FLAG){

                            totalGaveAmount += it.total_amount!!
                        }
                        if (it.give_take_flag == GET_ENTRY_FLAG){
                            totalGetAmount+= it.total_amount!!
                        }

                    }

                    if (it.ledgerCreatedByUID != User.userID){

                        if (it.give_take_flag == GAVE_ENTRY_FLAG){
                            totalGetAmount+= it.total_amount!!

                        }
                        if (it.give_take_flag == GET_ENTRY_FLAG){
                            totalGaveAmount += it.total_amount!!
                        }
                    }
                }

                var flag= false
                var totalAmount =0f
                if (totalGaveAmount>=totalGetAmount){
                    flag= GAVE_ENTRY_FLAG
                    totalAmount= totalGaveAmount- totalGetAmount
                }
                if (totalGetAmount>totalGaveAmount){
                    flag= GET_ENTRY_FLAG
                    totalAmount = totalGetAmount-totalGaveAmount
                }



                    if (flag == GAVE_ENTRY_FLAG){
                        binding.tvGiveMoneyFrag.setText("Rs. 00")
                        binding.tvGetMoneyFrag.setText("Rs. $totalAmount")

                    }
                    if (flag == GET_ENTRY_FLAG){
                        binding.tvGiveMoneyFrag.setText("Rs. $totalAmount")
                        binding.tvGetMoneyFrag.setText("Rs. 00")
                    }

        Log.d(TAG, "calculateTotalAmount: $totalAmount")
        adapter?.notifyDataSetChanged()
            }




    }
