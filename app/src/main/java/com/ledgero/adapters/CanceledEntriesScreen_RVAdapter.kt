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
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.ViewModels.CanceledEntriesViewModel
import com.ledgero.ViewModels.UnApprovedEntriesViewModel

class CanceledEntriesScreen_RVAdapter (context: Context, entries: ArrayList<Entries>?, viewModel: CanceledEntriesViewModel)
    :  RecyclerView.Adapter<CanceledEntriesScreen_RVAdapter.CanceledEntries_ViewHolder>()  {


    var context: Context
    var canceledEntries: ArrayList<Entries>?
    var viewModel=viewModel


    init {
        this.context= context
        this.canceledEntries= entries

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CanceledEntries_ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.canceled_entries_rv_layout, parent, false)

        return CanceledEntries_ViewHolder(view)

    }

    override fun onBindViewHolder(holder: CanceledEntries_ViewHolder, position: Int) {

        var entry=canceledEntries!!.get(position)


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





    }

    override fun getItemCount(): Int {

        return canceledEntries!!.size
    }

    inner class CanceledEntries_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var entryName: TextView
        var entryTimeStamp: TextView
        var entryMoney: TextView
//        var recevier_buttonLayout: LinearLayout
//        var acceptBtn: Button
//        var handshake: Button
       var requester_buttonLayout: LinearLayout
        var requestAgain: Button
        var deleteEntry: Button
        var giveTaleFlag: TextView
        var entryUID: String=""
        var mainLayout: ConstraintLayout

        init {
            entryName= itemView.findViewById(R.id.entry_title_tv_canceledEntries)
            entryTimeStamp= itemView.findViewById(R.id.entry_timeStamp_tv_canceledEntries)
            entryMoney= itemView.findViewById(R.id.entry_amount_tv_canceledEntries)
            giveTaleFlag = itemView.findViewById(R.id.entry_modeFlag_tv_canceledEntries)
            mainLayout= itemView.findViewById(R.id.main_layout_canceledEntries)

            /*
            this piece of code in onHold, still need to decide if we want to add this feature or not


            //reciver will see accept and handshake buttons
            //handshake button means they have solved the conflict and requesting to delete this entry
            recevier_buttonLayout= itemView.findViewById(R.id.buttons_layout_receiver_canceledEntries)
            acceptBtn= itemView.findViewById(R.id.bt_accept_receiver_canceledEntries)
            handshake= itemView.findViewById(R.id.bt_delete_request_receiver_canceledEntries)

            */


            //requester will see requestAgain button and delete button

            requester_buttonLayout= itemView.findViewById(R.id.buttons_layout_requester_canceledEntries)
            requestAgain= itemView.findViewById(R.id.bt_request_again_canceledEntries)
            deleteEntry= itemView.findViewById(R.id.bt_delete_canceledEntries)


            requestAgain.setOnClickListener {
                viewModel.requestAgain(adapterPosition)
            }

            deleteEntry.setOnClickListener {

                viewModel.deleteEntry(adapterPosition)
            }


        }

    }

}