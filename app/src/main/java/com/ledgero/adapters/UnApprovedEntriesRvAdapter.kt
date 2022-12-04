package com.ledgero.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.ViewModels.UnApprovedEntriesViewModel
import com.ledgero.fragments.ViewEntryInfoScreen
import com.ledgero.other.Constants
import com.ledgero.other.Constants.ADD_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.DELETE_REQUEST_REQUEST_MODE
import com.ledgero.other.Constants.EDIT_REQUEST_REQUEST_MODE
import com.ledgero.pushnotifications.PushNotification
import com.ledgero.utils.TimeFormatter
import java.util.*
import kotlin.collections.ArrayList


const val TAG = "UnApprovedEntRVAdapter"

class UnApprovedEntriesRvAdapter(
    context: Context,
    entries: ArrayList<Entries>?,
    val viewModel: UnApprovedEntriesViewModel,
    val listener: (frag: Fragment) -> Unit,
    val ledgerUID: String
) : RecyclerView.Adapter<UnApprovedEntriesRvAdapter.UnApprovedEntriesViewHolder>() {


    var context: Context
    var unApprovedEntries: ArrayList<Entries>?
    private val pushNotificationInterface = PushNotification()


    init {
        this.context = context
        this.unApprovedEntries = entries

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): UnApprovedEntriesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.unapproved_entries_rv_layout, parent, false)

        return UnApprovedEntriesViewHolder(view)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UnApprovedEntriesViewHolder, position: Int) {

        val entry = unApprovedEntries!![position]

        holder.entryName.text = entry.entry_title
        val date=Date(entry.entry_timeStamp!!)
        holder.entryTimeStamp.text = TimeFormatter.getFormattedTime(date.toString(),date)
        holder.entryMoney.text = entry.amount.toString()
        if (entry.originally_addedByUID.equals(User.userID)) {
            if (entry.give_take_flag!! == Constants.GAVE_ENTRY_FLAG) {
                holder.giveGetFlag.text = "You Gave"
                holder.entryMoney.setTextColor(Color.parseColor("#FF1010"))
            } else {
                holder.giveGetFlag.text = "You Got"

                holder.entryMoney.setTextColor(Color.parseColor("#166D0E"))

            }


        } else {

            if (entry.give_take_flag!! == Constants.GAVE_ENTRY_FLAG) {
                holder.giveGetFlag.text = "You Got"

                holder.entryMoney.setTextColor(Color.parseColor("#166D0E"))

            } else {
                holder.giveGetFlag.text = "You Gave"
                holder.entryMoney.setTextColor(Color.parseColor("#FF1010"))


            }

        }


        if (entry.entryMadeBy_userID.equals(User.userID)) {
            holder.requesterButtonLayout.visibility = View.VISIBLE
            holder.receiverButtonLayout.visibility = View.GONE
        } else {
            holder.requesterButtonLayout.visibility = View.GONE
            holder.receiverButtonLayout.visibility = View.VISIBLE

        }


        if (entry.requestMode == DELETE_REQUEST_REQUEST_MODE)//means user requested to delete this entry
        {
            holder.mainLayout.setBackgroundColor(Color.parseColor("#FFCCCB"))
        }

    }

    override fun getItemCount(): Int {

        return unApprovedEntries!!.size
    }

    inner class UnApprovedEntriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var entryName: TextView
        var entryTimeStamp: TextView
        var entryMoney: TextView
        var receiverButtonLayout: LinearLayout
        private var acceptBtnReceiver: Button
        private var rejectBtnReceiver: Button
        var requesterButtonLayout: LinearLayout
        private var waitBtnRequester: Button
        private var deleteBtnRequester: Button

        var giveGetFlag: TextView
        var entryUID: String = ""
        var mainLayout: ConstraintLayout

        init {
            entryName = itemView.findViewById(R.id.entry_title_tv_unApprovedEntries)
            entryTimeStamp = itemView.findViewById(R.id.entry_timeStamp_tv_unApprovedEntries)
            entryMoney = itemView.findViewById(R.id.entry_amount_tv_unApprovedEntries)
            giveGetFlag = itemView.findViewById(R.id.entry_modeFlag_tv_unApprovedEntries)
            mainLayout = itemView.findViewById(R.id.main_layout_unapprovedEntries)
            receiverButtonLayout =
                itemView.findViewById(R.id.buttons_layout_receiver_unapprovedEntries)
            acceptBtnReceiver = itemView.findViewById(R.id.bt_accept_receiver_unapprovedentries)
            rejectBtnReceiver = itemView.findViewById(R.id.bt_reject_receiver_unapprovedentries)
            requesterButtonLayout =
                itemView.findViewById(R.id.buttons_layout_requester_unapprovedEntries)
            waitBtnRequester = itemView.findViewById(R.id.bt_wait_requester_unapprovedentries)
            deleteBtnRequester = itemView.findViewById(R.id.bt_delete_requester_unapprovedentries)



            mainLayout.setOnClickListener {

                val frag= ViewEntryInfoScreen(unApprovedEntries!![adapterPosition],ledgerUID,true)

                listener(frag)
            }

            deleteBtnRequester.setOnClickListener {
                if (unApprovedEntries!![adapterPosition].requestMode == 1) {//addRequest
                    //means requester don't want receiver to see this entry Addition request in unapproved
                    // so delete the entry from unapproved
                    viewModel.deleteEntry(adapterPosition)
                }
                if (unApprovedEntries!![adapterPosition].requestMode == 2) {

                    viewModel.deleteUnApprovedEntryThenUpdateLedgerEntry(adapterPosition)
                }
            }
            waitBtnRequester.setOnClickListener {
                Toast.makeText(context, "Waiting For Approval", Toast.LENGTH_SHORT).show()
            }

            acceptBtnReceiver.setOnClickListener {

                // means receiver has accepted the request made by requester
                if (unApprovedEntries!![adapterPosition].requestMode == ADD_REQUEST_REQUEST_MODE)//request to add entry
                {
                    viewModel.approveEntry(adapterPosition)

                }
                if (unApprovedEntries!![adapterPosition].requestMode == DELETE_REQUEST_REQUEST_MODE) {//request to Delete entry
                    //means receiver accepted to delete a entry from ledger
                    viewModel.deleteEntryFromLedgerRequest_accepted(adapterPosition)
                }
                if (unApprovedEntries!![adapterPosition].requestMode == EDIT_REQUEST_REQUEST_MODE) {

                    var oldEntry = Entries()
                    val newEntry = unApprovedEntries!![adapterPosition]

                    for (currentLedger in User.getUserSingleLedgers()!!) {
                        if (currentLedger.ledgerUID!! == viewModel.mledgerUID) {
                            for (entry in currentLedger.entries!!) {
                                if (unApprovedEntries!![adapterPosition].entryUID!! == entry.entryUID) {
                                    oldEntry = entry
                                }
                            }
                        }
                    } // accessing entry to be deleted from ledger approved entries

                    viewModel.EditEntryaccepted(oldEntry, newEntry)

                }


                pushNotificationInterface.createAndSendNotification(viewModel.mledgerUID,
                    Constants.ENTRY_APPROVED)

            }

            rejectBtnReceiver.setOnClickListener {
                //means receiver did not accept the entry add/del request made by requester

                viewModel.rejectEntry(adapterPosition)
                pushNotificationInterface.createAndSendNotification(viewModel.mledgerUID,
                    Constants.ENTRY_REJECTED)
            }
        }

    }

}