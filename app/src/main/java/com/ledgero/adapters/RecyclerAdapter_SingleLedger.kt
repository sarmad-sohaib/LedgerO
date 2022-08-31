package com.ledgero.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.R

class RecyclerAdapter_SingleLedger (context: Context, entires: ArrayList<Entries>?): RecyclerView.Adapter<RecyclerAdapter_SingleLedger.MyViewHolder>()
{

    var context: Context
    var entries: ArrayList<Entries>?

    init {
        this.context= context
        this.entries= entires

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.individual_ledger_recycler_view, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      holder.title.text= entries!!.get(position).entry_title
        holder.timeStamp.text = entries!!.get(position).entry_timeStamp.toString()
        holder.amount.text= entries!!.get(position).amount.toString()
        if (entries!!.get(position).give_take_flag!!){
            holder.modeFlag.text= "You Get"
            holder.amount.setTextColor(Color.parseColor("#166D0E"))
        }else{
            holder.modeFlag.text= "You Gave"

            holder.amount.setTextColor(Color.parseColor("#FF1010"))

        }
    }

    override fun getItemCount(): Int {
    if (entries==null){
        return 0
    }else{
        return entries!!.size
    }

    }




    inner class  MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView
        var timeStamp: TextView
        var amount: TextView
        var modeFlag: TextView


        init {

            title= itemView.findViewById(R.id.entry_title_tv_indvidualLedger)
            timeStamp= itemView.findViewById(R.id.entry_timeStamp_tv_individualLedger)
            amount= itemView.findViewById(R.id.entry_amount_tv_indvidualLedger)
            modeFlag= itemView.findViewById(R.id.entry_modeFlag_tv_individualLedger)


        itemView.setOnClickListener(){

            Toast.makeText(context, "$title : $amount", Toast.LENGTH_SHORT).show()
        }
        }



    }
}