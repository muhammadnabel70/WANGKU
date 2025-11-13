package com.example.wangku.ui.profile // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wangku.TransactionRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: TransactionRepository) : ViewModel() {

    /**
     * [BERUBAH] Nama fungsi diganti agar lebih jelas.
     * Fungsi ini sekarang HANYA untuk menghapus data.
     */
    fun clearAllUserData() = viewModelScope.launch {
        // Panggil fungsi repository yang sudah "sadar" akan userId
        repository.clearAllTransactionsForUser()
    }
}