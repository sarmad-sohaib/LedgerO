package com.ledgero.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.fragments.IndividualLedgerScreen

class RecyclerViewAdapter(context: Context,singleLedgers: ArrayList<SingleLedgers>?):
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

 var context: Context
 var singleLedgers: ArrayList<SingleLedgers>?

init {
    this.context= context
    this.singleLedgers= singleLedgers

}



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_model, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.ledgerName.text = singleLedgers?.get(position)?.friend_userName
        holder.ledgerTimeStamp.text = "07:00 AM"
        holder.ledgerMoney.text = singleLedgers?.get(position)?.total_amount.toString()
        holder.ledgerUID=singleLedgers?.get(position)?.ledgerUID.toString()

    }

    override fun getItemCount(): Int {
        if (singleLedgers==null){
            return 0
        }else{
            return singleLedgers!!.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ledgerName: TextView
        var ledgerTimeStamp: TextView
        var ledgerMoney: TextView
        var ledgerDetail: TextView
        var ledgerNotification: Button
        var ledgerUID: String=""

        init {
            ledgerName = itemView.findViewById(R.id.tv_ledger_name)
            ledgerTimeStamp = itemView.findViewById(R.id.tv_time_stamp)
            ledgerMoney = itemView.findViewById(R.id.tv_money)
            ledgerDetail = itemView.findViewById(R.id.tv_detail)
            ledgerNotification = itemView.findViewById(R.id.bt_notification_send)

            itemView.setOnClickListener { v: View ->
                Toast.makeText(itemView.context, ledgerName.text, Toast.LENGTH_SHORT)
                    .show()

                var frag=IndividualLedgerScreen(ledgerUID)

                MainActivity.getMainActivityInstance().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_fragment_container_main, frag)
                    .addToBackStack(null)
                    .commit()

            }
        }
    }

}