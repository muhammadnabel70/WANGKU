package com.example.wangku // Sesuaikan package Anda

/**
 * Data class sederhana untuk menampung hasil query GROUP BY.
 * Ini bukan tabel database (bukan @Entity),
 * ini hanya struktur data untuk hasil.
 */
data class CategoryTotal(
    val category: String, // Nama kategori (misal "Food")
    val totalAmount: Double  // Total jumlah (misal 150000.0)
)