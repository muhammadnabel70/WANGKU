package com.example.wangku

/**
 * Data class generik untuk menampung hasil query GROUP BY.
 * Ini BUKAN tabel database.
 * 'label' bisa berisi: "Mon", "Tue" (Daily)
 * 'label' bisa berisi: "W45", "W46" (Weekly)
 * 'label' bisa berisi: "2025-11" (Monthly)
 * 'label' bisa berisi: "2025" (Yearly)
 */
data class ChartDataEntry(
    val label: String,
    val totalIncome: Double,
    val totalExpense: Double
)