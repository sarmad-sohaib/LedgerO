package com.ledgero.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.R

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){


    private val ledgerNames = arrayOf("Sarmad", "Sohaib")
    private val ledgerTimeStamps = arrayOf("Sarmad", "Sohaib")
    private val ledgersMoney = arrayOf("50", "40")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_model, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.ledgerName.text = ledgerNames[position]
        holder.ledgerTimeStamp.text = ledgerTimeStamps[position]
        holder.ledgerMoney.text = ledgersMoney[position]

    }

    override fun getItemCount(): Int {
        return ledgerNames.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var ledgerName: TextView
        var ledgerTimeStamp: TextView
        var ledgerMoney: TextView
        var ledgerDetail: TextView
        var ledgerNotification: Button

        init {
            ledgerName = itemView.findViewById(R.id.tv_ledger_name)
            ledgerTimeStamp = itemView.findViewById(R.id.tv_time_stamp)
            ledgerMoney = itemView.findViewById(R.id.tv_money)
            ledgerDetail = itemView.findViewById(R.id.tv_detail)
            ledgerNotification = itemView.findViewById(R.id.bt_notification_send)

            itemView.setOnClickListener { v: View ->
                Toast.makeText(itemView.context, adapterPosition.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}