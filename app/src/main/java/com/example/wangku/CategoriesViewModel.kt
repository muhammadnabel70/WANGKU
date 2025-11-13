package com.example.wangku.ui.categories // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wangku.Transaction
import com.example.wangku.TransactionRepository
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: TransactionRepository) : ViewModel() {

    // Fungsi ini tidak berubah. Repository yang akan menangani
    // penambahan userId dan penyimpanan ke Firestore.
    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
}