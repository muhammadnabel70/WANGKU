package com.example.wangku.ui.home // Sesuaikan package Anda

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wangku.FilterType
import com.example.wangku.Transaction
import com.example.wangku.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Sealed class untuk merepresentasikan item di RecyclerView (transaksi atau header tanggal)
sealed class DataItem {
    data class TransactionItem(val transaction: Transaction) : DataItem()
    data class DateHeaderItem(val date: String) : DataItem()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: TransactionRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val userId = firebaseAuth.currentUser?.uid

    // --- Data Saldo ---
    val totalIncome: Flow<Double?> = if (userId == null) emptyFlow() else {
        repository.getTotalIncome(userId)
    }

    val totalExpense: Flow<Double?> = if (userId == null) emptyFlow() else {
        repository.getTotalExpense(userId)
    }

    val totalBalance: Flow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        (income ?: 0.0) - (expense ?: 0.0)
    }

    // --- Logika Filter ---
    private val _filterState = MutableStateFlow(FilterType.MONTHLY_ALL)
    val filterState: StateFlow<FilterType> = _filterState
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))


    fun setFilter(filterType: FilterType) {
        _filterState.value = filterType
    }

    /**
     * Daftar transaksi yang reaktif terhadap filter, diurutkan terbaru dan dengan header tanggal
     */
    val allTransactions: Flow<List<DataItem>> = _filterState.flatMapLatest { filter ->
        if (userId == null) {
            emptyFlow() // Jika tidak ada user, kembalikan daftar kosong
        } else {
            val transactionsFlow = when (filter) {
                FilterType.DAILY -> {
                    val today = dateFormat.format(Date())
                    repository.getTransactionsBetweenDates(today, today, userId)
                }
                FilterType.WEEKLY -> {
                    val calendar = Calendar.getInstance()
                    val endDate = dateFormat.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_YEAR, -6)
                    val startDate = dateFormat.format(calendar.time)
                    repository.getTransactionsBetweenDates(startDate, endDate, userId)
                }
                FilterType.MONTHLY_ALL -> {
                    repository.getAllTransactions(userId)
                }
            }
            // Map transaksi ke DataItem dengan header tanggal
            transactionsFlow.map { transactionsList ->
                val items = mutableListOf<DataItem>()
                var lastDate: String? = null
                transactionsList.forEach { transaction ->
                    try {
                        // [PERBAIKAN] Parse string tanggal menjadi objek Date, lalu format kembali
                        val dateObject = dateFormat.parse(transaction.date)
                        val transactionDate = dateObject?.let { displayDateFormat.format(it) } ?: transaction.date

                        if (transactionDate != lastDate) {
                            items.add(DataItem.DateHeaderItem(transactionDate))
                            lastDate = transactionDate
                        }
                        items.add(DataItem.TransactionItem(transaction))
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error parsing date for transaction: ${transaction.id}", e)
                        // Jika ada error, tampilkan transaksi tanpa header tanggal untuk mencegah crash
                        items.add(DataItem.TransactionItem(transaction))
                    }
                }
                items
            }
        }
    }

    // --- Fungsi Aksi (Insert & Delete) ---

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    /**
     * [BARU] Fungsi untuk menghapus satu transaksi.
     */
    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    /**
     * Memulai sinkronisasi real-time saat ViewModel dibuat.
     */
    init {
        if (userId != null) {
            repository.startListeningForRemoteUpdates()
        }
    }
}