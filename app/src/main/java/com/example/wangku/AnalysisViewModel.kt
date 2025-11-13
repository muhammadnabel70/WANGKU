package com.example.wangku.ui.analysis // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import com.example.wangku.AnalysisFilterType
import com.example.wangku.ChartDataEntry
import com.example.wangku.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalysisViewModel(
    private val repository: TransactionRepository,
    private val firebaseAuth: FirebaseAuth // <-- 1. MINTA FirebaseAuth
) : ViewModel() {

    // --- Ambil ID Pengguna ---
    private val userId = firebaseAuth.currentUser?.uid

    // --- Data untuk Header ---
    // [BERUBAH] Semua Flow sekarang harus memeriksa userId
    val totalIncome: Flow<Double?> = if (userId == null) emptyFlow() else {
        repository.getTotalIncome(userId)
    }

    val totalExpense: Flow<Double?> = if (userId == null) emptyFlow() else {
        repository.getTotalExpense(userId)
    }

    val totalBalance: Flow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        (income ?: 0.0) - (expense ?: 0.0)
    }

    // --- LOGIKA FILTER GRAFIK ---

    private val _filterState = MutableStateFlow(AnalysisFilterType.MONTHLY)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setFilter(filterType: AnalysisFilterType) {
        _filterState.value = filterType
    }

    /**
     * [BERUBAH] chartData sekarang juga bergantung pada userId.
     */
    val chartData: Flow<List<ChartDataEntry>> = _filterState.flatMapLatest { filter ->
        if (userId == null) {
            emptyFlow() // Jika tidak ada user, kembalikan data kosong
        } else {
            // Jika ada user, jalankan query seperti biasa
            val calendar = Calendar.getInstance()
            val currentYear = yearFormat.format(calendar.time)

            when (filter) {
                AnalysisFilterType.DAILY -> {
                    val endDate = dateFormat.format(calendar.time) // Hari ini
                    calendar.add(Calendar.DAY_OF_YEAR, -6)
                    val startDate = dateFormat.format(calendar.time)
                    repository.getSummaryByDay(startDate, endDate, userId)
                }
                AnalysisFilterType.WEEKLY -> {
                    repository.getSummaryByWeek(currentYear, userId)
                }
                AnalysisFilterType.MONTHLY -> {
                    repository.getSummaryByMonth(currentYear, userId)
                }
                AnalysisFilterType.YEARLY -> {
                    repository.getSummaryByYear(userId)
                }
            }
        }
    }
}