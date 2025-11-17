package com.example.wangku.ui.categories // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wangku.TransactionRepository
import com.google.firebase.auth.FirebaseAuth

class CategoriesViewModelFactory(
    private val repository: TransactionRepository,
    private val firebaseAuth: FirebaseAuth // Tambahkan FirebaseAuth
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Teruskan FirebaseAuth ke ViewModel
            return CategoriesViewModel(repository, firebaseAuth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}