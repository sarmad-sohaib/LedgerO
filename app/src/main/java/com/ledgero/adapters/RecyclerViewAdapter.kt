package com.ledgero.adapters

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.MainActivity
import com.ledgero.R
import com.ledgero.fragments.IndividualLedgerScreen
import com.ledgero.other.Constants
import com.ledgero.other.Constants.GAVE_ENTRY_FLAG
import com.ledgero.other.Constants.GET_ENTRY_FLAG
import com.ledgero.utils.TimeFormatter
import java.util.*
import kotlin.collections.ArrayList

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.ledgerName.text = singleLedgers?.get(position)?.friend_userName
        holder.ledgerMoney.text = singleLedgers?.get(position)?.total_amount.toString()
        holder.ledgerUID=singleLedgers?.get(position)?.ledgerUID.toString()
        try {

           val date= Date(singleLedgers?.get(position)?.ledger_Created_timeStamp!!)
           holder.ledgerTimeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)

       }catch (e:Exception){
           //Toast.makeText(context, "Can't Load Single Ledgers${e.localizedMessage}", Toast.LENGTH_SHORT).show()

            Log.d(TAG, "onBindViewHolder: Can't Fetch Ledger Timestamp")
            holder.ledgerTimeStamp.text="DD/MM/YYYY"
       }

        if (singleLedgers?.get(position)?.ledgerCreatedByUID == User.userID){
            if (singleLedgers?.get(position)?.give_take_flag == GAVE_ENTRY_FLAG){

                holder.ledgerMoney.setTextColor(context.resources.getColor( R.color.red,null))
                holder.ledgerDetail.setText("You'll get")
            }
            if (singleLedgers?.get(position)?.give_take_flag== GET_ENTRY_FLAG){
                holder.ledgerMoney.setTextColor(context.resources.getColor( R.color.green,null))
                holder.ledgerDetail.setText("You'll give")
            }
        }
        if (singleLedgers?.get(position)?.ledgerCreatedByUID != User.userID){
            if (singleLedgers?.get(position)?.give_take_flag == GAVE_ENTRY_FLAG){
                holder.ledgerMoney.setTextColor(context.resources.getColor( R.color.green,null))
                holder.ledgerDetail.setText("You'll give")

            }
            if (singleLedgers?.get(position)?.give_take_flag== GET_ENTRY_FLAG){
                holder.ledgerMoney.setTextColor(context.resources.getColor( R.color.red,null))
                holder.ledgerDetail.setText("You'll get")
            }
        }

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
            ledgerName = itemView.findViewById(R.id.ledgerName)
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