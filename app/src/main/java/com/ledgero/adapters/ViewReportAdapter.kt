package com.ledgero.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.groupLedger.singleGroup.GroupEntry
import com.ledgero.other.Constants
import com.ledgero.other.Constants.CASH_IN
import com.ledgero.other.Constants.CASH_OUT
import com.ledgero.utils.TimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class ViewReportAdapter(context: Context, private val allEntries:ArrayList<Pair<Boolean,Any>>, private val isGroup:Boolean): RecyclerView.Adapter<ViewReportAdapter.MyViewHolder>()
{

    var context: Context

    init {
        this.context= context

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.report_layout_rv, parent, false)

        return MyViewHolder(view)
    }

    private fun setDataOnView(holder: MyViewHolder, position: Int, entry: Entries){
        holder.title.text= entry.entry_title
        val date = Date( entry.entry_timeStamp!!)
        holder.timeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)

        holder.amount.text= entry.amount.toString()

        if (entry.originally_addedByUID.equals(User.userID)){
            if (entry.give_take_flag!! == Constants.GAVE_ENTRY_FLAG){
                holder.modeFlag.text= "You Gave"
                holder.amount.setTextColor(Color.parseColor("#FF1010"))
            }else{
                holder.modeFlag.text= "You Got"

                holder.amount.setTextColor(Color.parseColor("#166D0E"))

            }


        }else{

            if (entry.give_take_flag!! == Constants.GAVE_ENTRY_FLAG){
                holder.modeFlag.text= "You Got"

                holder.amount.setTextColor(Color.parseColor("#166D0E"))

            }else{
                holder.modeFlag.text= "You Gave"
                holder.amount.setTextColor(Color.parseColor("#FF1010"))



            }

        }


    }

    private fun setGroupDataOnView(holder: MyViewHolder, position: Int, entry: GroupEntry){
        holder.title.text= entry.entry_title
        val date = Date( entry.entry_timeStamp!!)
        holder.timeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)

        holder.amount.text= entry.amount.toString()

            if (entry.cashInOutFlag!! == Constants.CASH_IN){
                holder.modeFlag.text= "CASH IN"
                holder.amount.setTextColor(Color.parseColor("#FF1010"))
            }else{
                holder.modeFlag.text= "CASH OUT"

                holder.amount.setTextColor(Color.parseColor("#166D0E"))

            }



    }

    private fun showHeader(name:String,holder: MyViewHolder, position: Int){
        holder.entryContainer.visibility=View.GONE
        holder.ledgerHeadingContainer.visibility= View.VISIBLE
        holder.ledgerName.text= name
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


        if (!isGroup){
        if (allEntries[position].first)// if true means its a entry
        {
            setDataOnView(holder,position, allEntries[position].second as Entries)
        }else{
            showHeader(allEntries[position].second as String,holder,position)
        }
        }else{

            if (allEntries[position].first)// if true means its a entry
            {
                setGroupDataOnView(holder,position, allEntries[position].second as GroupEntry)
            }else{
                showHeader(allEntries[position].second as String,holder,position)
            }

        }





    }

    override fun getItemCount(): Int {
        return allEntries.size ?: 0

    }



    inner class  MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView
        var timeStamp: TextView
        var amount: TextView
        var modeFlag: TextView
        var entryContainer: RelativeLayout
        var ledgerHeadingContainer: ConstraintLayout
        var ledgerName: TextView



        init {

            title= itemView.findViewById(R.id.entry_title_tv_report)
            timeStamp= itemView.findViewById(R.id.entry_timeStamp_tv_report)
            amount= itemView.findViewById(R.id.entry_amount_tv_report)
            modeFlag= itemView.findViewById(R.id.entry_modeFlag_tv_report)
            entryContainer = itemView.findViewById(R.id.entry_layout_report)
            ledgerHeadingContainer = itemView.findViewById(R.id.container_ledger_name)
            ledgerName= itemView.findViewById(R.id.ledgerName_report)


        }



    }
}