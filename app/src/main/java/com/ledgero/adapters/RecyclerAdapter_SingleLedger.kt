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
import com.ledgero.DataClasses.User
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.fragments.ViewEntryInfoScreen
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import java.util.*
import kotlin.collections.ArrayList

class RecyclerAdapter_SingleLedger (context: Context, entires: ArrayList<Entries>?,ledgerUID:String): RecyclerView.Adapter<RecyclerAdapter_SingleLedger.MyViewHolder>()
{

    var context: Context
    var entries: ArrayList<Entries>?
    var currentLedgerUID: String

    init {
        this.context= context
        this.entries= entires
        this.currentLedgerUID=ledgerUID

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.individual_ledger_recycler_view, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      holder.title.text= entries!!.get(position).entry_title
        var date = Date( entries!!.get(position).entry_timeStamp!!)
        holder.timeStamp.text = date.toString()

        holder.amount.text= entries!!.get(position).amount.toString()

        if (entries!!.get(position).originally_addedByUID.equals(User.userID)){
            if (entries!!.get(position).give_take_flag!! == GAVE_ENTRY_FLAG){
                holder.modeFlag.text= "You Gave"
                holder.amount.setTextColor(Color.parseColor("#FF1010"))
            }else{
                holder.modeFlag.text= "You Got"

                holder.amount.setTextColor(Color.parseColor("#166D0E"))

            }


        }else{

            if (entries!!.get(position).give_take_flag!! == GAVE_ENTRY_FLAG){
                holder.modeFlag.text= "You Got"

                holder.amount.setTextColor(Color.parseColor("#166D0E"))

            }else{
                holder.modeFlag.text= "You Gave"
                holder.amount.setTextColor(Color.parseColor("#FF1010"))



            }

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

            Toast.makeText(context, "${title.text} : ${amount.text}", Toast.LENGTH_SHORT).show()

            var frag= ViewEntryInfoScreen(entries!!.get(adapterPosition),currentLedgerUID)


            MainActivity.getMainActivityInstance().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_fragment_container_main, frag)
                .addToBackStack(null)
                .commit()


        }
        }



    }
}
