package com.ledgero.reminders.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.databinding.ItemReminderBinding
import com.ledgero.reminders.reminders.data.Reminder
import java.text.DateFormat

class ReminderListAdapter (
    private val onItemClick: OnItemClick
        ): ListAdapter<Reminder, ReminderListAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder (private val binding: ItemReminderBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val reminder = getItem(position)
                        onItemClick.onReminderClick(reminder)
                    }
                }
            }
        }

        fun bind(reminder: Reminder) {
            binding.apply {
                textViewReminderAmount.text = reminder.amount
                textViewReminderDescription.text = reminder.description
                textViewReminderRecipient.text = reminder.recipient
                textViewReminderStatus.text = if (reminder.give == true) "Give to:" else "Take from:"
                textViewReminderTimeStamp.text = DateFormat.getDateTimeInstance().format(reminder.timeStamp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class DiffCallBack: DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean = oldItem == newItem

    }
}

interface OnItemClick {
    fun onReminderClick(reminder: Reminder)
}