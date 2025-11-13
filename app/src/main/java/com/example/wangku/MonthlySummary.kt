package com.example.wangku // Sesuaikan package Anda

/**
 * Data class untuk menampung hasil query GROUP BY bulan.
 * Ini BUKAN tabel database.
 */
data class MonthlySummary(
    val monthYear: String, // Format: "2025-11"
    val totalIncome: Double,
    val totalExpense: Double
)