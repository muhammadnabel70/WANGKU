package com.example.wangku

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "transactions")
data class Transaction(

    @PrimaryKey
    @ColumnInfo(name = "id")
    @get:Exclude // <-- 1. PINDAHKAN ANOTASI @Exclude KE SINI
    var id: String = "", // Beri nilai default

    @ColumnInfo(name = "userId")
    var userId: String = "",

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "date")
    var date: String = "", // Format "yyyy-MM-dd"

    @ColumnInfo(name = "category")
    var category: String = "",

    @ColumnInfo(name = "amount")
    var amount: Double = 0.0,

    @ColumnInfo(name = "type")
    var type: TransactionType = TransactionType.EXPENSE,

    @ColumnInfo(name = "icon_name")
    var iconName: String = "",

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0L,

    @ColumnInfo(name = "note") // [BARU] Kolom untuk pesan/catatan
    var note: String = "" // Nilai default string kosong
)

enum class TransactionType {
    INCOME, EXPENSE
}