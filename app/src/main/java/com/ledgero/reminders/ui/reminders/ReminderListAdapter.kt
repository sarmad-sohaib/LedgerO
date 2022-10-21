package com.ledgero.reminders.ui.reminders

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.databinding.ItemReminderBinding
import com.ledgero.reminders.data.Reminder
import java.text.DateFormat

class ReminderListAdapter(
    private val onItemClick: OnItemClick
) : ListAdapter<Reminder, ReminderListAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val reminder = getItem(position)
                        onItemClick.onReminderClick(reminder)
                    }
                }

                checkBoxReminderIsComplete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        if (checkBoxReminderIsComplete.isChecked) {
                            val reminder = getItem(position)
                            onItemClick.onReminderCompleteCheckBoxClick(reminder, true)
                            Log.i("cb", ": true")
                        } else {
                            val reminder = getItem(position)
                            onItemClick.onReminderCompleteCheckBoxClick(reminder, false)
                            Log.i("cb", ": false")
                        }
                    }
                }
            }
        }

        fun bind(reminder: Reminder) {
            binding.apply {
                textViewReminderAmount.text = reminder.amount
                textViewReminderDescription.text = reminder.description
                textViewReminderRecipient.text = reminder.recipient
                textViewReminderStatus.text =
                    if (reminder.give == true) "Give to:" else "Take from:"
                textViewReminderTimeStamp.text =
                    DateFormat.getDateTimeInstance().format(reminder.timeStamp)
                checkBoxReminderIsComplete.isChecked = reminder.complete == true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemReminderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
            oldItem == newItem

    }
}

interface OnItemClick {
    fun onReminderClick(reminder: Reminder)
    fun onReminderCompleteCheckBoxClick(reminder: Reminder, checkboxValue: Boolean)
}