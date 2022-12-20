package com.ledgero.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ledgero.daos.IndividualScreenDAO
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.Repositories.IndividualScreenRepo
import com.ledgero.ViewModelFactories.IndividualScreenViewModeFactory
import com.ledgero.ViewModels.IndividualScreenViewModel
import com.ledgero.adapters.RecyclerAdapter_SingleLedger
import com.ledgero.customDialogs.DeleteLedgerCustomDialog
import com.ledgero.other.Constants.ADD_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.DELETE_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.EDIT_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG
import com.ledgero.other.Constants.NO_REQUEST_REQUEST_MODE
import kotlinx.android.synthetic.main.fragment_individual_ledger_screen.view.*


private const val TAG= "IndividLedgerScreen"
class IndividualLedgerScreen(val ledgerUID:String) : Fragment() {

     var currentSelectedLedgerUID:String =""
    var currentSelectLedger: SingleLedgers? =null
lateinit     var viewModel: IndividualScreenViewModel
lateinit var entries: ArrayList<Entries>
lateinit var gaveText : TextView
lateinit var getText : TextView
    private var dbReference = FirebaseDatabase.getInstance().reference


    private var layoutManager: RecyclerView.LayoutManager? = null



    init {
        currentSelectedLedgerUID=ledgerUID+""



        for (i in User.getUserSingleLedgers()!!){

            if (i.ledgerUID.equals(ledgerUID)){
                currentSelectLedger=i

            }
        }
        instanceObject=this

    }

    companion object{
        var adapter: RecyclerView.Adapter<RecyclerAdapter_SingleLedger.MyViewHolder>? = null
        var instanceObject: IndividualLedgerScreen? =null

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val  view= inflater.inflate(R.layout.fragment_individual_ledger_screen, container, false)


        //View Model Init

        val dao= IndividualScreenDAO(currentSelectedLedgerUID)
        val repo = IndividualScreenRepo(dao)
        viewModel= ViewModelProvider(this, IndividualScreenViewModeFactory(repo))
            .get(IndividualScreenViewModel::class.java)


//check if there is delete request
        dbReference.child("deleteLedgerRequests").child(ledgerUID).get().addOnCompleteListener {
            if (it.isSuccessful){
                if (it.result.exists()){
                    val friendName = it.result.child("requesterName").value.toString()
                    val friendUID = it.result.child("deleteRequestBy").value.toString()

                    if (friendUID != User.userID!!){
                        val deleteDialog= DeleteLedgerCustomDialog(ledgerUID,friendName,friendUID, isLedgerDeleted,requireContext())
                        deleteDialog.isCancelable=false
                        deleteDialog.show(childFragmentManager,"deleteLedger")

                    }


                }
            }
        }



        //View Model Init ---Ends Here

         gaveText = view.findViewById<TextView>(R.id.tv_give_money_individScreen)
        getText = view.findViewById<TextView>(R.id.tv_get_money_individScreen)


        viewModel.getLedgerGiveTakeFlag().observe(viewLifecycleOwner,Observer{

            if(it==null){
                currentSelectLedger!!.give_take_flag=null
            }
            if(it==false){
                currentSelectLedger!!.give_take_flag=false
            }
            if(it==true){
                currentSelectLedger!!.give_take_flag=true
            }
        })


        //Check if give_take flag is null then it means ledger Amount is clear 00 for both give and take
        // if give_take flag is true then it means the user who created ledger will get money..means he will get
        // if give_take flag is false then it means user who created ledger will owe/give money..mean he will give
        //now if current user is not the user who created ledger then we will reverse these give and gave
        //so each time we will check flag and we will check if this user is the one who created or the other one
        viewModel.getLedgerTotalAmount().observe(viewLifecycleOwner,Observer{

            if(currentSelectLedger!!.give_take_flag==null){
                gaveText.text="Rs. 00"
                getText.text="Rs. 00"
                currentSelectLedger!!.total_amount=it

            }
            if(currentSelectLedger!!.give_take_flag== GAVE_ENTRY_FLAG){

                if(User.userID.equals(currentSelectLedger!!.ledgerCreatedByUID)){
                    //means we will set the amount to get (You'll Get) variable for this user
                    getText.text="Rs. $it"
                    gaveText.text= "Rs. 00"

                }else{
                    //means we will set this amount to  gave (You'll Give)
                    getText.text="Rs. 00"
                    gaveText.text= "Rs. $it"

                }

                currentSelectLedger!!.total_amount=it

            }
            if(currentSelectLedger!!.give_take_flag== GET_ENTRY_FLAG){


                if(User.userID.equals(currentSelectLedger!!.ledgerCreatedByUID)){
                    //means we will set the amount to gave (You'll Give) variable for this user
                    getText.text="Rs. 00"
                    gaveText.text= "Rs. $it"

                }else{
                    //means we will set this amount to  get (You'll Get)
                    getText.text="Rs. $it"
                    gaveText.text= "Rs. 00"

                }

                currentSelectLedger!!.total_amount=it


            }

        })



        val gotButton= view.bt_got_individScreen
        val gaveButton= view.bt_gave_individScreen
        val rv = view.findViewById<RecyclerView>(R.id.rv_ledgers_individualScreen)
        layoutManager = LinearLayoutManager(context)
        rv.layoutManager = layoutManager
     try {
         adapter= RecyclerAdapter_SingleLedger(requireContext(),currentSelectLedger!!.entries, currentSelectedLedgerUID)
         rv.adapter= adapter
     }catch (e:Exception){
         Log.d("IndividualLedger", "onCreateView: ${e.message}")
     }


        getTouchHelper(rv).attachToRecyclerView(rv)
        viewModel.getEntries().observe(viewLifecycleOwner, Observer{

            try{
                currentSelectLedger!!.entries=it /* = java.util.ArrayList<com.ledgero.DataClasses.Entries> */
                adapter= RecyclerAdapter_SingleLedger(requireContext(),currentSelectLedger!!.entries,currentSelectedLedgerUID)
                rv.adapter= adapter
                entries=it}catch (e:Exception){
                Log.d("IndividualLedger", "onCreateView: ${e.message}")
                }
        })
        gotButton.setOnClickListener {
            //1 will inidcate that user clicked got button
          setFragmentResult("addEntryBtn", bundleOf("mode" to GET_ENTRY_FLAG,"ledger" to currentSelectLedger))
           parentFragmentManager
               .beginTransaction()
               .addToBackStack(null)
               .replace(R.id.fl_fragment_container_main,AddNewEntryDetail(currentSelectLedger!!,false,null))
               .commit()

        }
        gaveButton.setOnClickListener {
            //0 will indicate that user clicked gave button
            setFragmentResult("addEntryBtn", bundleOf("mode" to GAVE_ENTRY_FLAG,"ledger" to currentSelectLedger))
            parentFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fl_fragment_container_main,AddNewEntryDetail(currentSelectLedger!!,false,null))
                .commit()

        }




        val unApprovedBtn=view.bt_unapproved_entriest_individScreen
        val canceledEntries=view.bt_canceled_entries_individScreen

        canceledEntries.setOnClickListener {
            val frag = CanceledEntriesScreen(currentSelectedLedgerUID)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fl_fragment_container_main,frag)
                .addToBackStack(null)
                .commit()
        }


        unApprovedBtn.setOnClickListener {

            val frag= UnApprovedEntriesScreen(currentSelectedLedgerUID)

            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fl_fragment_container_main,frag)
                .addToBackStack(null)
                .commit()
        }


        viewModel.stopListeningForUnApprovedEntriesCount()
        viewModel.stopListeningForCancelledEntries()

            val unApprovedNotifyIcon= view.findViewById<TextView>(R.id.unApprovedEntriesNotify_TV_individScreen)

            viewModel.startListeningForUnApprovedEntriesCount().observe(viewLifecycleOwner, Observer {
                if (it!=null){
                if (it>0){
                    unApprovedNotifyIcon.text=it.toString()
                    unApprovedNotifyIcon.visibility=View.VISIBLE
                    viewModel.vibratePhoneForNewUnApprovedEntry()
                    val animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)

                    unApprovedNotifyIcon.startAnimation(animationZoomOut)
                    Handler(Looper.getMainLooper()).postDelayed({
                        unApprovedNotifyIcon.clearAnimation()
                    },500)
                }
                if (it>99){
                    unApprovedNotifyIcon.text="99+"
                    unApprovedNotifyIcon.visibility=View.VISIBLE
                    val animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)
                    viewModel.vibratePhoneForNewUnApprovedEntry()
                    unApprovedNotifyIcon.startAnimation(animationZoomOut)

                    Handler(Looper.getMainLooper()).postDelayed({
                        unApprovedNotifyIcon.clearAnimation()
                    },500)
                }
                if (it<=0){
                    unApprovedNotifyIcon.text="0"
                    unApprovedNotifyIcon.visibility=View.GONE
                }
            }
            })



        val canceledNotifyIcon= view.findViewById<TextView>(R.id.cacneledEntriesNotify_TV_individScreen)

        viewModel.startListeningForCancelledEntries().observe(viewLifecycleOwner, Observer {
            if (it!=null){
                if (it>0){
                    canceledNotifyIcon.text=it.toString()
                    canceledNotifyIcon.visibility=View.VISIBLE
                    viewModel.vibratePhoneForNewUnApprovedEntry()
                    val animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)

                    canceledNotifyIcon.startAnimation(animationZoomOut)
                    Handler(Looper.getMainLooper()).postDelayed({
                        canceledNotifyIcon.clearAnimation()
                    },500)
                }
                if (it>99){
                    canceledNotifyIcon.text="99+"
                    canceledNotifyIcon.visibility=View.VISIBLE
                    val animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)
                    viewModel.vibratePhoneForNewUnApprovedEntry()
                    canceledNotifyIcon.startAnimation(animationZoomOut)
                    Handler(Looper.getMainLooper()).postDelayed({
                        canceledNotifyIcon.clearAnimation()
                    },500)
                }
                if (it<=0){
                    canceledNotifyIcon.text="0"
                    canceledNotifyIcon.visibility=View.GONE
                }
            }
        })





        return view
    }

    override fun onPause() {


        removeTotalAmountListener()

        viewModel.stopListeningForUnApprovedEntriesCount()
        viewModel.stopListeningForCancelledEntries()
        super.onPause()
    }



    override fun onDestroy() {

        viewModel.stopListeningForUnApprovedEntriesCount()
        viewModel.stopListeningForCancelledEntries()
        viewModel.removeListener()
        viewModel.removeLedgerMetaDataListener()
        super.onDestroy()
    }



    override fun onResume() {
        setTotalAmountListener()
        parentFragmentManager.clearBackStack("addEntry")
        super.onResume()
    }


    //this will enable left/right swipes on RV
    fun getTouchHelper(rv: RecyclerView):ItemTouchHelper{

        val callBack:ItemTouchHelper.SimpleCallback =object :ItemTouchHelper.
        SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
  return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position= viewHolder.adapterPosition
if (direction==ItemTouchHelper.LEFT){

    getDeleteDialougeBox(position,rv).show()

}
                if (direction==ItemTouchHelper.RIGHT){

//                    getEditDialougeBox(position,rv).show()

                    Toast.makeText(context, "Swipe Left to delete the entry", Toast.LENGTH_SHORT).show()
                    rv.adapter!!.notifyDataSetChanged()
                }

            }


        }

val itemTouchHelper: ItemTouchHelper= ItemTouchHelper(callBack)


return itemTouchHelper

    }


    fun getDeleteDialougeBox(pos: Int, rv: RecyclerView): AlertDialog.Builder{

       val dialog= AlertDialog.Builder(this.context)

        dialog.setTitle("Deleting Ledger Entry")
            .setMessage("Are you sure to delete entry!")
            .setCancelable(true)
            .setPositiveButton("Yes Delete it"){dialogInterface,it->
                //delete entry

                val entry= entries.get(pos)
                if (entry.requestMode== NO_REQUEST_REQUEST_MODE){
                    viewModel.deleteEntry(pos)
                    dialogInterface.cancel()
                    val frag = UnApprovedEntriesScreen(currentSelectedLedgerUID)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_main,frag)
                        .commit()
                }else{
                    if (entry.requestMode==ADD_REQUEST_REQUEST_MODE
                        ||entry.requestMode== DELETE_REQUEST_REQUEST_MODE
                        ||entry.requestMode== EDIT_REQUEST_REQUEST_MODE
                    ){
                        Toast.makeText(context, "Already in request. Please Check Un-Approved Entries", Toast.LENGTH_SHORT).show()
                  rv.adapter!!.notifyDataSetChanged()
                    }
                }




            }
            .setNegativeButton("No"){dialogInterface,it->
                //cancel it
            dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
    return dialog
    }

    fun getEditDialougeBox(pos: Int, rv: RecyclerView): AlertDialog.Builder{

        val dialog= AlertDialog.Builder(this.context)

        dialog.setTitle("Edit Ledger Entry")
            .setMessage("Are you sure to Edit entry!")
            .setCancelable(true)
            .setPositiveButton("Yes Delete it"){dialogInterface,it->
                //delete entry


                rv.adapter!!.notifyDataSetChanged()
            }
            .setNegativeButton("No"){dialogInterface,it->
                //cancel it
                dialogInterface.cancel()
                rv.adapter!!.notifyDataSetChanged()

            }
        return dialog
    }



    fun updateLedgerTotalAmount(){

        Log.d(TAG, "updateLedgerTotalAmount: called")
         var dbReference = FirebaseDatabase.getInstance().reference


        dbReference.child("ledgerEntries").child(ledgerUID).get()
            .addOnCompleteListener {


                val entries= ArrayList<Entries>()
                if (it.isSuccessful){
                    if (it.result.exists()){
                        it.result.children.forEach{ entry ->
                            val e=  entry.getValue(Entries::class.java)
                            entries.add(e!!)
                        }
                        calculateTotalAmount(entries)
                    }else{
                        calculateTotalAmount(entries)

                    }
                }else{
                    calculateTotalAmount(entries)
                }

            }.addOnCanceledListener {
                calculateTotalAmount(entries)
            }.addOnFailureListener{
                calculateTotalAmount(entries)
            }


    }

    private fun calculateTotalAmount(entries: ArrayList<Entries>){
        var dbReference = FirebaseDatabase.getInstance().reference
        Log.d(TAG, "calculation: ")

        if (entries.size<=0){
            gaveText.setText("Rs. 00")
            getText.setText("Rs. 00")
            Log.d(TAG, "calculateTotalAmount: 00")
            val map = mapOf("give_take_flag" to false, "total_amount" to 0 , "total_entries" to 0)
            dbReference.child("ledgerInfo").child(ledgerUID).updateChildren(map)
            return
        }


        dbReference.child("ledgerInfo").child(ledgerUID).child("ledgerCreatedByUID").get().addOnCompleteListener {
            if (it.isSuccessful){

                val ledgerCreatedBy= it.result.value.toString()

                var totalGaveAmount= 0f
                var totalGetAmount = 0f

                entries.forEach{
                    if (it.originally_addedByUID == ledgerCreatedBy){

                        if (it.give_take_flag == GAVE_ENTRY_FLAG){

                            totalGaveAmount += it.amount!!
                        }
                        if (it.give_take_flag == GET_ENTRY_FLAG){
                            totalGetAmount+= it.amount!!
                        }

                    }

                    if (it.originally_addedByUID != ledgerCreatedBy){

                        if (it.give_take_flag == GAVE_ENTRY_FLAG){
                            totalGetAmount+= it.amount!!

                        }
                        if (it.give_take_flag == GET_ENTRY_FLAG){
                            totalGaveAmount += it.amount!!
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

                if (User.userID == ledgerCreatedBy){

                    if (flag == GAVE_ENTRY_FLAG){
                        gaveText.setText("Rs. 00")
                        getText.setText("Rs. $totalAmount")

                    }
                    if (flag == GET_ENTRY_FLAG){
                        gaveText.setText("Rs. $totalAmount")
                        getText.setText("Rs. 00")
                    }
                }
                else{
                    if (flag == GAVE_ENTRY_FLAG){

                        gaveText.setText("Rs. $totalAmount")
                        getText.setText("Rs. 00")

                    }
                    if (flag == GET_ENTRY_FLAG){
                        gaveText.setText("Rs. 00")
                        getText.setText("Rs. $totalAmount")

                    }
                }


                val map = mapOf("give_take_flag" to flag, "total_amount" to totalAmount , "total_entries" to entries.size)
                dbReference.child("ledgerInfo").child(ledgerUID).updateChildren(map)
            }
        }



    }

    private val amountListener = object :ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){


                    updateLedgerTotalAmount()
                }
        }

        override fun onCancelled(error: DatabaseError) = Unit

    }

    private fun setTotalAmountListener() {
        var dbReference = FirebaseDatabase.getInstance().reference

        dbReference.child("ledgerInfo").child(ledgerUID).child("ledgerCreatedByUID")
            .addValueEventListener(amountListener)
    }
    private fun removeTotalAmountListener(){
        var dbReference = FirebaseDatabase.getInstance().reference

        dbReference.child("ledgerInfo").child(ledgerUID).child("ledgerCreatedByUID").removeEventListener(amountListener)
    }


    val isLedgerDeleted= fun(flag:Boolean){
        if (flag){
            parentFragmentManager.popBackStack()
        }
    }
}