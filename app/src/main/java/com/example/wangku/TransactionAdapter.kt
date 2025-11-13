package com.example.wangku // Sesuaikan package Anda

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wangku.databinding.ListItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.lang.Exception

/**
 * [BERUBAH] Adapter sekarang menerima lambda 'onItemLongClick'
 */
class TransactionAdapter(
    private val onItemLongClick: (Transaction) -> Unit // Lambda untuk long click
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    /**
     * ViewHolder: Menyimpan referensi ke view di dalam setiap item
     */
    inner class TransactionViewHolder(val binding: ListItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ListItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)

        // [BARU] Tambahkan listener 'setOnLongClickListener'
        holder.itemView.setOnLongClickListener {
            onItemLongClick(transaction) // Panggil lambda
            true // 'true' berarti kita sudah menangani event ini
        }

        holder.binding.apply {
            tvTitle.text = transaction.title
            tvDate.text = formatDisplayDate(transaction.date) // Format tanggal
            tvCategoryName.text = transaction.category

            ivCategoryIcon.setImageResource(
                getDrawableIdByName(holder.itemView.context, transaction.iconName)
            )

            // Atur jumlah dan warnanya berdasarkan tipe transaksi
            if (transaction.type == TransactionType.INCOME) {
                tvAmount.text = "Rp${transaction.amount}" // TODO: Format mata uang
                val incomeColor = ContextCompat.getColor(holder.itemView.context, R.color.brand_green)
                tvAmount.setTextColor(incomeColor)
            } else {
                tvAmount.text = "-Rp${transaction.amount}" // TODO: Format mata uang
                tvAmount.setTextColor(Color.RED) // Sebaiknya definisikan di colors.xml
            }
        }
    }

    /**
     * Helper function untuk format tanggal dari "yyyy-MM-dd"
     * menjadi "dd MMM yyyy" (misal: "13 Nov 2025")
     */
    private fun formatDisplayDate(dbDate: String): String {
        return try {
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = dbFormat.parse(dbDate)
            displayFormat.format(date!!)
        } catch (e: Exception) {
            dbDate // Jika parsing gagal, tampilkan apa adanya
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
class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id // Cek berdasarkan ID unik
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem // Cek semua field
    }
}