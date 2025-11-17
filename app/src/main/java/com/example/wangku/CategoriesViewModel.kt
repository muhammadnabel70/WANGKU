package com.example.wangku.ui.categories // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wangku.Transaction
import com.example.wangku.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CategoriesViewModel(
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

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
}