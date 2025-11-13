package com.example.wangku // Sesuaikan dengan nama package Anda

import androidx.annotation.DrawableRes

data class Category(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int,
    val type: CategoryType // Untuk membedakan Income atau Expense
)

enum class CategoryType {
    INCOME, EXPENSE
}