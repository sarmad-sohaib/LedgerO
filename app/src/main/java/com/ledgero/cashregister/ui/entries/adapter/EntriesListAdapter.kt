package com.ledgero.cashregister.ui.entries.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.R
import com.ledgero.cashregister.data.Entry
import com.ledgero.databinding.ItemCashRegisterEntryBinding
import java.text.DateFormat

class EntriesListAdapter (
    private val listener: OnItemClick
        ) : ListAdapter<Entry, EntriesListAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemCashRegisterEntryBinding):
    RecyclerView.ViewHolder(binding.root){

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition

                    if (position != RecyclerView.NO_POSITION) {
                        val entry = getItem(position)
                        listener.onItemClick(entry)
                    }
                }
            }
        }

        fun bind(entry: Entry) {
            binding.apply {
                textViewEntryAmount.text = entry.amount
                textViewEntryDescription.text = entry.description
                textViewEntryStatus.text = if (entry.out == true) "Out" else "In"
                textViewEntryTimeStamp.text = DateFormat.getDateTimeInstance().format(entry.dateTimeStamp)

                if (entry.out == true) textViewEntryAmount.setTextColor(ContextCompat.getColor(textViewEntryAmount.context, R.color.red))
                else if (entry.out == false) textViewEntryAmount.setTextColor(ContextCompat.getColor(textViewEntryAmount.context, R.color.green))
            }
        }
    }

    class DiffCallBack: DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean = oldItem == newItem

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCashRegisterEntryBinding.inflate(
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

    interface OnItemClick {
        fun onItemClick(entry: Entry)
    }
}