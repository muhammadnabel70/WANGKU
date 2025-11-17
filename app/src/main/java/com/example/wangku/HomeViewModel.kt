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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class DataItem {
    data class TransactionItem(val transaction: Transaction) : DataItem()
    data class DateHeaderItem(val date: String) : DataItem()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: TransactionRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // [PERBAIKAN] Gunakan "GUEST_USER" jika tidak ada pengguna yang login
    private val userId = firebaseAuth.currentUser?.uid ?: "GUEST_USER"

    // --- Data Saldo ---
    val totalIncome: Flow<Double?> = repository.getTotalIncome(userId)
    val totalExpense: Flow<Double?> = repository.getTotalExpense(userId)

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

    val allTransactions: Flow<List<DataItem>> = _filterState.flatMapLatest { filter ->
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

        transactionsFlow.map { transactionsList ->
            val items = mutableListOf<DataItem>()
            var lastDate: String? = null
            transactionsList.forEach { transaction ->
                try {
                    val dateObject = dateFormat.parse(transaction.date)
                    val transactionDate = dateObject?.let { displayDateFormat.format(it) } ?: transaction.date

                    if (transactionDate != lastDate) {
                        items.add(DataItem.DateHeaderItem(transactionDate))
                        lastDate = transactionDate
                    }
                    items.add(DataItem.TransactionItem(transaction))
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error parsing date for transaction: ${transaction.id}", e)
                    items.add(DataItem.TransactionItem(transaction))
                }
            }
            items
        }
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    init {
        // [PERBAIKAN] Hanya sinkronkan jika bukan tamu
        if (firebaseAuth.currentUser != null) {
            repository.startListeningForRemoteUpdates()
        }
    }
}