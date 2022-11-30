package com.ledgero.groupLedger.recyclerViews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.GroupLedgers
import com.ledgero.R
import com.ledgero.utils.TimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class GroupAdapter(
   private val context: Context,
    private val groups: ArrayList<GroupLedgers>?
):RecyclerView.Adapter<GroupAdapter.ViewHolder>()
{


 inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
 {
     val groupName: TextView
     val groupTimeStamp: TextView
     val groupMoney: TextView
     val groupDetail: TextView
     private val groupNotification: Button

     init {
         groupName = itemView.findViewById(R.id.groupName)
         groupTimeStamp = itemView.findViewById(R.id.group_tv_time_stamp)
         groupMoney = itemView.findViewById(R.id.group_tv_money)
         groupDetail = itemView.findViewById(R.id.group_tv_detail)
         groupNotification = itemView.findViewById(R.id.group_bt_notification_send)

         itemView.setOnClickListener {
             Toast.makeText(context, groupName.text, Toast.LENGTH_SHORT)
                 .show()


         }
     }
 }
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
          .inflate(R.layout.recycler_layout_group_ledger_fragment, parent, false)

      return ViewHolder(view)
  }

  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val group = groups!![position]


      holder.groupName.text = group.groupName
      holder.groupMoney.text = "NN:Rs"
      holder.groupDetail.text="NN:NN"

      val date= Date(group.createdTimeStamp.toLong())
      holder.groupTimeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)


  }

  override fun getItemCount(): Int {
return groups?.size ?: 0

  }
}
