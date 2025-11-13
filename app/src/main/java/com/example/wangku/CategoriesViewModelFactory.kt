package com.example.wangku.ui.categories // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wangku.TransactionRepository

class CategoriesViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}