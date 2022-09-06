package com.ledgero.adapters

import android.content.Context
import android.graphics.Color
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
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.ViewModels.UnApprovedEntriesViewModel

class UnApprovedEntries_RVAdapter(context: Context, entries: ArrayList<Entries>?,viewModel: UnApprovedEntriesViewModel)
    :  RecyclerView.Adapter<UnApprovedEntries_RVAdapter.UnApprovedEntries_ViewHolder>()  {


    var context: Context
    var unApprovedEntries: ArrayList<Entries>?
    var viewModel=viewModel

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
        holder.entryTimeStamp.text= entry.entry_timeStamp.toString()
        holder.entryMoney.text= entry.amount.toString()
        if (entry.give_take_flag!!){
            holder.giveTaleFlag.text= "You Get"
            holder.giveTaleFlag.setTextColor(Color.parseColor("#166D0E"))
            holder.entryMoney.setTextColor(Color.parseColor("#166D0E"))
        }else{
            holder.giveTaleFlag.text= "You Gave"
            holder.giveTaleFlag.setTextColor(Color.parseColor("#FF1010"))
            holder.entryMoney.setTextColor(Color.parseColor("#FF1010"))
        }

        if (entry.entryMadeBy_userID.equals(User.userID)){
          holder.requester_buttonLayout.visibility=View.VISIBLE
            holder.receiver_buttonLayout.visibility=View.GONE
        }else{
            holder.requester_buttonLayout.visibility=View.GONE
            holder.receiver_buttonLayout.visibility=View.VISIBLE

        }


        if (entry.requestMode==2)//means user requested to add this entry
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

        var giveTaleFlag: TextView
        var entryUID: String=""
        var mainLayout: ConstraintLayout

        init {
            entryName= itemView.findViewById(R.id.entry_title_tv_unApprovedEntries)
            entryTimeStamp= itemView.findViewById(R.id.entry_timeStamp_tv_unApprovedEntries)
            entryMoney= itemView.findViewById(R.id.entry_amount_tv_unApprovedEntries)
            giveTaleFlag = itemView.findViewById(R.id.entry_modeFlag_tv_unApprovedEntries)
            mainLayout= itemView.findViewById(R.id.main_layout_unapprovedEntries)
            receiver_buttonLayout= itemView.findViewById(R.id.buttons_layout_receiver_unapprovedEntries)
            acceptBtn_receiver= itemView.findViewById(R.id.bt_accept_receiver_unapprovedentries)
            rejectBtn_receiver= itemView.findViewById(R.id.bt_reject_receiver_unapprovedentries)
            requester_buttonLayout= itemView.findViewById(R.id.buttons_layout_requester_unapprovedEntries)
            waitBtn_requester=itemView.findViewById(R.id.bt_wait_requester_unapprovedentries)
            deleteBtn_requester= itemView.findViewById(R.id.bt_delete_requester_unapprovedentries)



            deleteBtn_requester.setOnClickListener(){
                if (unApprovedEntries!!.get(adapterPosition).requestMode==1){
                    //delete the entry from unapproved
                    viewModel.deleteEntry(adapterPosition)
                }
                if (unApprovedEntries!!.get(adapterPosition).requestMode==2){

                    viewModel.deleteUnApprovedEntryThenUpdateLedgerEntry(adapterPosition)
                }
            }
            waitBtn_requester.setOnClickListener(){
                Toast.makeText(context, "Waiting For Approval", Toast.LENGTH_SHORT).show()
            }

            acceptBtn_receiver.setOnClickListener(){

                if (unApprovedEntries!!.get(adapterPosition).requestMode==1)//request to add entry
                {  viewModel.approveEntry(adapterPosition)}
                if (unApprovedEntries!!.get(adapterPosition).requestMode==2){
                    viewModel.deleteEntry(adapterPosition)
                }
            }

            rejectBtn_receiver.setOnClickListener(){


                viewModel.rejectEntry(adapterPosition)
            }
        }

    }

}