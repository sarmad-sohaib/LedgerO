package com.ledgero.groupLedger.recyclerViews

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.GroupLedgersInfo
import com.ledgero.R
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.other.Constants.CASH_IN
import com.ledgero.utils.TimeFormatter
import java.util.*
import kotlin.collections.ArrayList


private const val TAG= "GroupAdapter"
class GroupAdapter(
    private val context: Context,
    private val groups: ArrayList<GroupInfo>,
    private val singleGroupClick: (GroupInfo) -> Unit
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
             singleGroupClick(groups!![adapterPosition])

         }
     }
 }
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
          .inflate(R.layout.recycler_layout_group_ledger_fragment, parent, false)

      return ViewHolder(view)
  }

  @RequiresApi(Build.VERSION_CODES.M)
  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      try {
          val group = groups!![position]


          holder.groupName.text = group.groupName
          holder.groupMoney.text = group.groupTotalAmount.toString()
          if (group.cashInOut == CASH_IN){
              holder.groupDetail.text= "Cash In"
          }else{
              holder.groupDetail.text= "Cash Out"
                holder.groupMoney.setTextColor(context.getColor(R.color.red))
          }

          val date= Date(group.groupCreateServerTimestamp.toLong())
          holder.groupTimeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)

      }catch (e:Exception){
          Log.d(TAG, "onBindViewHolder: ${e.message} ")
      }

  }

  override fun getItemCount(): Int {
return groups?.size ?: 0

  }
}
