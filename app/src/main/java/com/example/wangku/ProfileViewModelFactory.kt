package com.example.wangku.ui.profile // Sesuaikan package Anda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wangku.TransactionRepository

class ProfileViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}