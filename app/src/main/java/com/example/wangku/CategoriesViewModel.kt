package com.example.wangku.ui.categories // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wangku.Transaction
import com.example.wangku.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
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

    // Fungsi ini tidak berubah. Repository yang akan menangani
    // penambahan userId dan penyimpanan ke Firestore.
    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
}