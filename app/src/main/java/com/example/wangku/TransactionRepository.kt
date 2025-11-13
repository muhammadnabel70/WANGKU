package com.example.wangku

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    // --- Mendapatkan ID Pengguna Saat Ini ---
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    // --- Operasi MENULIS (Write) ---

    /**
     * Menyimpan transaksi baru ke Firestore dan Room.
     */
    suspend fun insert(transaction: Transaction) {
        val userId = currentUserId
        if (userId == null) {
            Log.e("TransactionRepo", "User not logged in, cannot insert transaction")
            return
        }

        try {
            // Buat dokumen baru di Firestore
            val documentRef = firestore.collection("users").document(userId)
                .collection("transactions").document() // Buat ID unik

            // Set ID dan userId di objek transaksi
            transaction.id = documentRef.id // Ambil ID unik dari Firestore
            transaction.userId = userId

            // Simpan ke Firestore (Cloud)
            documentRef.set(transaction).await()

            // Simpan ke Room (Lokal)
            transactionDao.insertOrReplace(transaction)

        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error inserting transaction", e)
        }
    }

    /**
     * [BARU] Menghapus satu transaksi dari Room dan Firestore.
     */
    suspend fun delete(transaction: Transaction) {
        val userId = currentUserId
        if (userId == null || transaction.id.isEmpty()) {
            Log.e("TransactionRepo", "Cannot delete, user or transaction ID is missing")
            return
        }

        try {
            // 1. Hapus dari Firestore (Cloud)
            firestore.collection("users").document(userId)
                .collection("transactions").document(transaction.id)
                .delete()
                .await()

            // 2. Hapus dari Room (Lokal)
            transactionDao.delete(transaction)

        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error deleting transaction", e)
        }
    }

    /**
     * Menghapus semua data pengguna saat logout.
     */
    suspend fun clearAllTransactionsForUser() {
        val userId = currentUserId ?: return

        // 1. Hapus dari Room
        transactionDao.deleteAllTransactionsForUser(userId)

        // 2. Hapus dari Firestore (Batch delete)
        try {
            val querySnapshot = firestore.collection("users").document(userId)
                .collection("transactions").get().await()

            val batch = firestore.batch()
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error deleting transactions from Firestore", e)
        }
    }

    // --- Operasi MEMBACA (Read) ---
    // Fungsi-fungsi ini HANYA membaca dari Room (database lokal).

    fun getAllTransactions(userId: String): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions(userId)
    }

    fun getTransactionsBetweenDates(startDate: String, endDate: String, userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate, userId)
    }

    fun getTotalIncome(userId: String): Flow<Double?> {
        return transactionDao.getTotalIncome(userId)
    }

    fun getTotalExpense(userId: String): Flow<Double?> {
        return transactionDao.getTotalExpense(userId)
    }

    fun getSummaryByDay(startDate: String, endDate: String, userId: String): Flow<List<ChartDataEntry>> {
        return transactionDao.getSummaryByDay(startDate, endDate, userId)
    }

    fun getSummaryByWeek(year: String, userId: String): Flow<List<ChartDataEntry>> {
        return transactionDao.getSummaryByWeek(year, userId)
    }

    fun getSummaryByMonth(year: String, userId: String): Flow<List<ChartDataEntry>> {
        return transactionDao.getSummaryByMonth(year, userId)
    }

    fun getSummaryByYear(userId: String): Flow<List<ChartDataEntry>> {
        return transactionDao.getSummaryByYear(userId)
    }

    /**
     * Memulai "mendengarkan" perubahan di Firestore secara real-time.
     */
    fun startListeningForRemoteUpdates() {
        val userId = currentUserId
        if (userId == null) {
            Log.e("TransactionRepo", "No user to listen for updates.")
            return
        }

        firestore.collection("users").document(userId)
            .collection("transactions")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("TransactionRepo", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                CoroutineScope(Dispatchers.IO).launch {
                    for (docChange in snapshots.documentChanges) {
                        try {
                            val transaction = docChange.document.toObject<Transaction>()
                            transaction.id = docChange.document.id

                            when (docChange.type) {
                                // Data baru atau berubah di cloud
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    transactionDao.insertOrReplace(transaction)
                                }
                                // Data dihapus di cloud
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    transactionDao.delete(transaction)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TransactionRepo", "Error parsing document", e)
                        }
                    }
                }
            }
    }
}