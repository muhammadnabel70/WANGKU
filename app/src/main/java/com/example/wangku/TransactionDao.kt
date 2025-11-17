package com.example.wangku // Sesuaikan package Anda

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /**
     * Menyisipkan atau Mengganti (Replace) transaksi.
     * Penting untuk sinkronisasi dari Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(transaction: Transaction)

    /**
     * [BARU] Menghapus satu transaksi.
     * Room akan mengidentifikasinya berdasarkan Primary Key (@PrimaryKey).
     */
    @Delete
    suspend fun delete(transaction: Transaction)

    // --- Query untuk Home ---

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, timestamp DESC")
    fun getAllTransactions(userId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND userId = :userId ORDER BY date DESC, timestamp DESC")
    fun getTransactionsBetweenDates(startDate: String, endDate: String, userId: String): Flow<List<Transaction>>

    // --- Query untuk Saldo ---

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND userId = :userId")
    fun getTotalIncome(userId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND userId = :userId")
    fun getTotalExpense(userId: String): Flow<Double?>

    // --- 4 QUERY UNTUK GRAFIK ANALISIS ---

    /** 1. GROUP BY HARI (DAILY) */
    @Query("""
        SELECT
            strftime('%Y-%m-%d', date) as label,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate AND userId = :userId
        GROUP BY label
        ORDER BY label ASC
    """)
    fun getSummaryByDay(startDate: String, endDate: String, userId: String): Flow<List<ChartDataEntry>>

    /** 2. GROUP BY MINGGU (WEEKLY) */
    @Query("""
        SELECT
            strftime('%Y-W%W', date) as label,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions
        WHERE strftime('%Y', date) = :year AND userId = :userId
        GROUP BY label
        ORDER BY label ASC
    """)
    fun getSummaryByWeek(year: String, userId: String): Flow<List<ChartDataEntry>>

    /** 3. GROUP BY BULAN (MONTHLY) */
    @Query("""
        SELECT
            strftime('%Y-%m', date) as label,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions
        WHERE strftime('%Y', date) = :year AND userId = :userId
        GROUP BY label
        ORDER BY label ASC
    """)
    fun getSummaryByMonth(year: String, userId: String): Flow<List<ChartDataEntry>>

    /** 4. GROUP BY TAHUN (YEARLY) */
    @Query("""
        SELECT
            strftime('%Y', date) as label,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions
        WHERE userId = :userId
        GROUP BY label
        ORDER BY label ASC
    """)
    fun getSummaryByYear(userId: String): Flow<List<ChartDataEntry>>

    // --- Query Hapus ---

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactionsForUser(userId: String)
}