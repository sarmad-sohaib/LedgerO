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
import java.util.*
import kotlin.collections.ArrayList

// Extra spaces
class RecyclerViewAdapter(context: Context, singleLedgers: ArrayList<SingleLedgers>?) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    // Indented with tabs maybe, more like this in this class
    var context: Context
    var singleLedgers: ArrayList<SingleLedgers>?

    init {
        this.context = context
        this.singleLedgers = singleLedgers

    }

    // Too many blank lines
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_model, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.apply {
            ledgerTimeStamp.text = Date(12324521).toString()

            singleLedgers?.get(position)?.let { ledger ->
                ledgerName.text = ledger.friend_userName
                ledgerMoney.text = ledger.total_amount.toString()
                ledgerUID = ledger.ledgerUID.toString()
            }
        }
    }

    // Don't use !! except in tests
    // ?: is called elvis operator
    override fun getItemCount(): Int = singleLedgers?.size ?: 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ledgerName: TextView
        var ledgerTimeStamp: TextView
        var ledgerMoney: TextView
        var ledgerDetail: TextView
        var ledgerNotification: Button
        var ledgerUID: String = ""

        init {
            ledgerName = itemView.findViewById(R.id.tv_ledger_name)
            ledgerTimeStamp = itemView.findViewById(R.id.tv_time_stamp)
            ledgerMoney = itemView.findViewById(R.id.tv_money)
            ledgerDetail = itemView.findViewById(R.id.tv_detail)
            ledgerNotification = itemView.findViewById(R.id.bt_notification_send)

            itemView.setOnClickListener {
                Toast.makeText(
                    itemView.context,
                    ledgerName.text,
                    Toast.LENGTH_SHORT
                ).show()

                // From name, not clear what frag is
                val frag = IndividualLedgerScreen(ledgerUID)

                // Good formatting
                MainActivity.getMainActivityInstance().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_fragment_container_main, frag)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}

// Good simple class.