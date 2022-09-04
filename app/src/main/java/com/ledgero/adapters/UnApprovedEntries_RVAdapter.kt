package com.ledgero.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
            holder.buttonLayout.visibility= View.GONE
                holder.waitingText.visibility= View.VISIBLE
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
        var buttonLayout: LinearLayout
        var waitingText: TextView
        var acceptBtn: Button
        var rejectBtn: Button
        var giveTaleFlag: TextView
        var entryUID: String=""
        var mainLayout: ConstraintLayout

        init {
            entryName= itemView.findViewById(R.id.entry_title_tv_unApprovedEntries)
            entryTimeStamp= itemView.findViewById(R.id.entry_timeStamp_tv_unApprovedEntries)
            entryMoney= itemView.findViewById(R.id.entry_amount_tv_unApprovedEntries)
            buttonLayout= itemView.findViewById(R.id.buttons_layout_unapprovedEntries)
            waitingText= itemView.findViewById(R.id.waitingText_tv_unapprovedEntries)
            acceptBtn= itemView.findViewById(R.id.bt_accept_unapprovedentries)
            rejectBtn= itemView.findViewById(R.id.bt_reject_unapprovedentries)
            giveTaleFlag = itemView.findViewById(R.id.entry_modeFlag_tv_unApprovedEntries)
            mainLayout= itemView.findViewById(R.id.main_layout_unapprovedEntries)



            acceptBtn.setOnClickListener(){

                if (unApprovedEntries!!.get(adapterPosition).requestMode==1)//request to add entry
                {  viewModel.approveEntry(adapterPosition)}
                if (unApprovedEntries!!.get(adapterPosition).requestMode==2){
                    viewModel.deleteEntry(adapterPosition)
                }
            }

            rejectBtn.setOnClickListener(){
                viewModel.rejectEntry(adapterPosition)
            }
        }

    }

}