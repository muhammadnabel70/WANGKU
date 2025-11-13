package com.example.wangku // Sesuaikan dengan package Anda

import androidx.room.TypeConverter

class Converters {

    // Konverter untuk TransactionType (Enum)
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name // Mengubah INCOME -> "INCOME"
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value) // Mengubah "INCOME" -> INCOME
    }

    // Kita juga bisa tambahkan konverter lain di sini jika perlu
    // misalnya untuk tipe data Date, dll.
}