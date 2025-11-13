package com.example.wangku.ui.home // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wangku.TransactionRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * [BERUBAH] Factory ini sekarang juga membutuhkan FirebaseAuth
 * untuk diteruskan ke HomeViewModel.
 */
class HomeViewModelFactory(
    private val repository: TransactionRepository,
    private val firebaseAuth: FirebaseAuth // <-- 1. TAMBAHKAN INI
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 2. Teruskan firebaseAuth ke constructor ViewModel
            return HomeViewModel(repository, firebaseAuth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}