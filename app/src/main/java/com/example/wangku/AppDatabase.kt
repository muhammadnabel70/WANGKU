package com.example.wangku // Sesuaikan dengan package Anda

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// 1. Anotasi @Database: Memberi tahu Room bahwa ini adalah kelas Database
@Database(
    entities = [Transaction::class], // Daftar semua tabel (Entity) kita
    version = 1, // Versi database, naikkan jika Anda mengubah skema
    exportSchema = false // Kita tidak perlu mengekspor skema (opsional)
)
@TypeConverters(Converters::class) // 2. Memberi tahu Room untuk menggunakan Converter kita
abstract class AppDatabase : RoomDatabase() {

    // 3. Fungsi abstract untuk setiap DAO
    abstract fun transactionDao(): TransactionDao

    // 4. Companion object untuk membuat Singleton
    companion object {

        // @Volatile: Memastikan nilai INSTANCE selalu up-to-date
        // dan terlihat oleh semua thread.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Fungsi untuk mendapatkan instance database (Singleton)
        fun getInstance(context: Context): AppDatabase {
            // 'synchronized' berarti hanya satu thread yang bisa
            // menjalankan kode ini pada satu waktu.
            // Ini mencegah pembuatan dua database secara tidak sengaja.
            synchronized(this) {
                var instance = INSTANCE

                // Jika database belum ada, buat yang baru
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "wangku_database" // Nama file database Anda
                    )
                        // .fallbackToDestructiveMigration() // Opsi jika Anda menaikkan versi
                        .build()

                    INSTANCE = instance
                }

                // Kembalikan instance yang sudah ada atau yang baru dibuat
                return instance
            }
        }
    }
}