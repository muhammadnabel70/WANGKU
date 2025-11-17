package com.example.wangku // Sesuaikan package Anda

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wangku.databinding.ListItemTransactionBinding
import com.example.wangku.ui.home.DataItem
import com.example.wangku.ui.home.DataItem.DateHeaderItem
import com.example.wangku.ui.home.DataItem.TransactionItem
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private val currencyFormatter: NumberFormat, // [BARU] Tambahkan formatter
    private val onItemLongClick: (Transaction) -> Unit
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DataItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DateHeaderItem -> VIEW_TYPE_HEADER
            is TransactionItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val binding = ListItemTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TransactionViewHolder(binding, onItemLongClick)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> {
                val dateHeaderItem = getItem(position) as DateHeaderItem
                holder.bind(dateHeaderItem.date)
            }
            is TransactionViewHolder -> {
                val transactionItem = getItem(position) as TransactionItem
                holder.bind(transactionItem.transaction)
            }
        }
    }

    inner class TransactionViewHolder(
        val binding: ListItemTransactionBinding,
        private val onItemLongClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            itemView.setOnLongClickListener {
                onItemLongClick(transaction)
                true
            }

            binding.apply {
                tvTitle.text = transaction.title
                tvCategoryName.text = transaction.note // [PERUBAHAN] Tampilkan note di sini

                ivCategoryIcon.setImageResource(
                    getDrawableIdByName(itemView.context, transaction.iconName)
                )

                if (transaction.type == TransactionType.INCOME) {
                    tvAmount.text = currencyFormatter.format(transaction.amount)
                    val incomeColor = ContextCompat.getColor(itemView.context, R.color.blue_income)
                    tvAmount.setTextColor(incomeColor)
                } else {
                    tvAmount.text = "-${currencyFormatter.format(transaction.amount)}"
                    tvAmount.setTextColor(Color.RED)
                }
            }
        }
    }

    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDateHeader: TextView = view.findViewById(R.id.tv_date_header)

        fun bind(date: String) {
            tvDateHeader.text = date
        }
    }

    private fun getDrawableIdByName(context: Context, iconName: String): Int {
        return context.resources.getIdentifier(
            iconName,
            "drawable",
            context.packageName
        ).let { if (it == 0) R.drawable.ic_launcher_background else it }
    }
}

class DataItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return when {
            oldItem is TransactionItem && newItem is TransactionItem -> oldItem.transaction.id == newItem.transaction.id
            oldItem is DateHeaderItem && newItem is DateHeaderItem -> oldItem.date == newItem.date
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}