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
import java.text.SimpleDateFormat
import java.util.Locale
import java.lang.Exception

class TransactionAdapter(
    private val onItemLongClick: (Transaction) -> Unit // Lambda untuk long click
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
                TransactionViewHolder(binding, onItemLongClick) // Pass the long click listener
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

    /**
     * ViewHolder untuk menampilkan transaksi individual.
     */
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
                // tvDate.text = formatDisplayDate(transaction.date) // Date is now in header
                tvCategoryName.text = transaction.category

                ivCategoryIcon.setImageResource(
                    getDrawableIdByName(itemView.context, transaction.iconName)
                )

                if (transaction.type == TransactionType.INCOME) {
                    tvAmount.text = "Rp${transaction.amount}" // TODO: Format mata uang
                    val incomeColor = ContextCompat.getColor(itemView.context, R.color.blue_income)
                    tvAmount.setTextColor(incomeColor)
                } else {
                    tvAmount.text = "-Rp${transaction.amount}" // TODO: Format mata uang
                    tvAmount.setTextColor(Color.RED) // Sebaiknya definisikan di colors.xml
                }
            }
        }
    }

    /**
     * ViewHolder untuk menampilkan header tanggal.
     */
    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDateHeader: TextView = view.findViewById(R.id.tv_date_header)

        fun bind(date: String) {
            tvDateHeader.text = date
        }
    }

    /**
     * Helper function untuk mendapatkan ID drawable dari namanya (String).
     */
    private fun getDrawableIdByName(context: Context, iconName: String): Int {
        return context.resources.getIdentifier(
            iconName,         // Nama resource (misal: "ic_makanan")
            "drawable",       // Tipe resource
            context.packageName // Package aplikasi
        )
            .let { if (it == 0) R.drawable.ic_launcher_background else it } // Ganti dengan ikon default
    }
}

/**
 * DiffUtil: Menghitung perbedaan data agar RecyclerView tahu
 * item mana yang berubah, ditambah, atau dihapus (lebih efisien).
 */
class DataItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return when {
            oldItem is TransactionItem && newItem is TransactionItem -> oldItem.transaction.id == newItem.transaction.id
            oldItem is DateHeaderItem && newItem is DateHeaderItem -> oldItem.date == newItem.date
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem // Data classes handle content equality automatically
    }
}