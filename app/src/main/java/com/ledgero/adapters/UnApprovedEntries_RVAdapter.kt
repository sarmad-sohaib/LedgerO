package com.ledgero.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.ViewModels.UnApprovedEntriesViewModel
import com.ledgero.other.Constants
import com.ledgero.other.Constants.ADD_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.DELETE_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.EDIT_REQUEST_REQUEST_MODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class UnApprovedEntries_RVAdapter(context: Context, entries: ArrayList<Entries>?,viewModel: UnApprovedEntriesViewModel)
    :  RecyclerView.Adapter<UnApprovedEntries_RVAdapter.UnApprovedEntries_ViewHolder>()  {


    var context: Context
    var unApprovedEntries: ArrayList<Entries>?
    var viewModel=viewModel

    var TAG = "UnApprovedEntries_RVAdapter"

    init {
        this.context= context
        this.unApprovedEntries= entries

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): UnApprovedEntries_ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.unapproved_entries_rv_layout, parent, false)

        return UnApprovedEntries_ViewHolder(view)

    }

    override fun onBindViewHolder(holder: UnApprovedEntries_ViewHolder, position: Int) {

    var entry=unApprovedEntries!!.get(position)

        holder.entryName.text= entry.entry_title
        holder.entryTimeStamp.text= Date( entry.entry_timeStamp!!).toString()
        holder.entryMoney.text= entry.amount.toString()
        if (entry.originally_addedByUID.equals(User.userID)){
            if (entry.give_take_flag!! == Constants.GAVE_ENTRY_FLAG){
                holder.giveGetFlag.text= "You Gave"
                holder.entryMoney.setTextColor(Color.parseColor("#FF1010"))
            }else{
                holder.giveGetFlag.text= "You Got"

                holder.entryMoney.setTextColor(Color.parseColor("#166D0E"))

            }


        }else{

            if (entry.give_take_flag!! == Constants.GAVE_ENTRY_FLAG){
                holder.giveGetFlag.text= "You Got"

                holder.entryMoney.setTextColor(Color.parseColor("#166D0E"))

            }else{
                holder.giveGetFlag.text= "You Gave"
                holder.entryMoney.setTextColor(Color.parseColor("#FF1010"))



            }

        }


        if (entry.entryMadeBy_userID.equals(User.userID)){
          holder.requester_buttonLayout.visibility=View.VISIBLE
            holder.receiver_buttonLayout.visibility=View.GONE
        }else{
            holder.requester_buttonLayout.visibility=View.GONE
            holder.receiver_buttonLayout.visibility=View.VISIBLE

        }


        if (entry.requestMode== DELETE_REQUEST_REQUEST_MODE)//means user requested to delete this entry
        {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#FFCCCB"))
        }

    }

    override fun getItemCount(): Int {

        return unApprovedEntries!!.size
    }

    inner class UnApprovedEntries_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var entryName: TextView
        var entryTimeStamp: TextView
        var entryMoney: TextView
        var receiver_buttonLayout: LinearLayout
        var acceptBtn_receiver: Button
        var rejectBtn_receiver: Button
        var requester_buttonLayout: LinearLayout
        var waitBtn_requester: Button
        var deleteBtn_requester: Button

        var giveGetFlag: TextView
        var entryUID: String=""
        var mainLayout: ConstraintLayout

        init {
            entryName= itemView.findViewById(R.id.entry_title_tv_unApprovedEntries)
            entryTimeStamp= itemView.findViewById(R.id.entry_timeStamp_tv_unApprovedEntries)
            entryMoney= itemView.findViewById(R.id.entry_amount_tv_unApprovedEntries)
            giveGetFlag = itemView.findViewById(R.id.entry_modeFlag_tv_unApprovedEntries)
            mainLayout= itemView.findViewById(R.id.main_layout_unapprovedEntries)
            receiver_buttonLayout= itemView.findViewById(R.id.buttons_layout_receiver_unapprovedEntries)
            acceptBtn_receiver= itemView.findViewById(R.id.bt_accept_receiver_unapprovedentries)
            rejectBtn_receiver= itemView.findViewById(R.id.bt_reject_receiver_unapprovedentries)
            requester_buttonLayout= itemView.findViewById(R.id.buttons_layout_requester_unapprovedEntries)
            waitBtn_requester=itemView.findViewById(R.id.bt_wait_requester_unapprovedentries)
            deleteBtn_requester= itemView.findViewById(R.id.bt_delete_requester_unapprovedentries)



            deleteBtn_requester.setOnClickListener {
                if (unApprovedEntries!!.get(adapterPosition).requestMode==1){//addRequest
                    //means requester don't want receiver to see this entry Addition request in unapproved
                    // so delete the entry from unapproved
                    viewModel.deleteEntry(adapterPosition)
                }
                if (unApprovedEntries!!.get(adapterPosition).requestMode==2){

                    viewModel.deleteUnApprovedEntryThenUpdateLedgerEntry(adapterPosition)
                }
            }
            waitBtn_requester.setOnClickListener {
                Toast.makeText(context, "Waiting For Approval", Toast.LENGTH_SHORT).show()
            }

            acceptBtn_receiver.setOnClickListener {

                // means reciver has accepted the request made by requester
                 if (unApprovedEntries!!.get(adapterPosition).requestMode==ADD_REQUEST_REQUEST_MODE)//request to add entry
                {
                    viewModel.approveEntry(adapterPosition)

                }
                if (unApprovedEntries!!.get(adapterPosition).requestMode== DELETE_REQUEST_REQUEST_MODE){//request to Delete entry
                    //means reciever accepted to delete a entry from ledger
                    viewModel.deleteEntryFromLedgerRequest_accepted(adapterPosition)
                }
                if (unApprovedEntries!!.get(adapterPosition).requestMode== EDIT_REQUEST_REQUEST_MODE){

                    var oldentry= Entries();
                    var newEntry= unApprovedEntries!!.get(adapterPosition)

                    for (currentLedger in User.getUserSingleLedgers()!!){
                        if (currentLedger.ledgerUID!!.equals(viewModel.mledgerUID)){
                            for (entry in currentLedger.entries!!){
                                if (unApprovedEntries!!.get(adapterPosition).entryUID!!.equals(entry.entryUID)){
                                    oldentry=entry;
                                }
                            }
                        }
                    } // accessing entry to be deleted from ledger approved entries

                    viewModel.EditEntryaccepted(oldentry,newEntry)

                }
            }

            rejectBtn_receiver.setOnClickListener {
                //means reciver did not accept the entry add/del request made by requester

                viewModel.rejectEntry(adapterPosition)
            }
        }

    }

}