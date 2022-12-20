package com.ledgero.groupLedger.singleGroup

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.adapters.TAG
import com.ledgero.fragments.ViewEntryInfoScreen
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.other.Constants.CASH_IN
import com.ledgero.other.Constants.CASH_OUT
import com.ledgero.utils.TimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class SingleGroupAdapter(val context: Context, private val groupEntries: ArrayList<GroupEntry>?
, val groupInfo: GroupInfo):
    RecyclerView.Adapter<SingleGroupAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_group_ledger_rv_adapter, parent, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ledgerName.text = groupEntries?.get(position)?.entry_title
        holder.ledgerMoney.text = groupEntries?.get(position)?.amount.toString()
        holder.ledgerUID=groupEntries?.get(position)?.entryUID.toString()
        try {

            val date= Date(groupEntries?.get(position)?.entry_timeStamp!!)
            holder.ledgerTimeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)

        }catch (e:Exception){
            //Toast.makeText(context, "Can't Load Single Ledgers${e.localizedMessage}", Toast.LENGTH_SHORT).show()

            Log.d(TAG, "onBindViewHolder: Can't Fetch Ledger Timestamp")
            holder.ledgerTimeStamp.text="DD/MM/YYYY"
        }

     if (groupEntries?.get(position)!!.cashInOutFlag==  CASH_IN){
         holder.ledgerDetail.text= context.getText(R.string.cash_in)
         holder.ledgerMoney.setTextColor(context.getColor(R.color.green))
     }
        if (groupEntries.get(position)!!.cashInOutFlag==  CASH_OUT){
            holder.ledgerDetail.text= context.getText(R.string.cash_out)
            holder.ledgerMoney.setTextColor(context.getColor(R.color.red))
        }
    }

    override fun getItemCount() = groupEntries!!.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ledgerName: TextView
        var ledgerTimeStamp: TextView
        var ledgerMoney: TextView
        var ledgerDetail: TextView
        var ledgerUID: String=""

        init {
            ledgerName = itemView.findViewById(R.id.group_entry_title)
            ledgerTimeStamp = itemView.findViewById(R.id.entry_timeStamp_tv_groupLedger)
            ledgerMoney = itemView.findViewById(R.id.entry_amount_tv_singleGroupLedger)
            ledgerDetail = itemView.findViewById(R.id.entry_modeFlag_tv_groupLedger)
            //      ledgerNotification = itemView.findViewById(R.id.bt_notification_send)

            itemView.setOnClickListener { v: View ->
                Toast.makeText(itemView.context, ledgerName.text, Toast.LENGTH_SHORT)
                    .show()

                var frag= ViewEntryInfoScreen(groupEntries!![adapterPosition], groupInfo.groupUID,false,groupInfo)

                MainActivity.getMainActivityInstance().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_fragment_container_main, frag)
                    .addToBackStack(null)
                    .commit()

            }
        }
    }

}