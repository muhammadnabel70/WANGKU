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
import java.util.UUID

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    // --- Mendapatkan ID Pengguna Saat Ini ---
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    // --- Operasi MENULIS (Write) ---

    suspend fun insert(transaction: Transaction) {
        val userId = currentUserId

        // Menangani logika penyimpanan untuk pengguna yang masuk dan tamu
        if (userId != null) {
            // Pengguna yang masuk: simpan ke Firestore dan Room
            try {
                val documentRef = firestore.collection("users").document(userId)
                    .collection("transactions").document()
                transaction.id = documentRef.id
                transaction.userId = userId
                transaction.timestamp = System.currentTimeMillis()

                documentRef.set(transaction).await()
                transactionDao.insertOrReplace(transaction)
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error inserting transaction for logged-in user", e)
            }
        } else {
            // Mode Tamu: hanya simpan ke Room
            try {
                transaction.id = UUID.randomUUID().toString() // Buat ID unik untuk tamu
                transaction.userId = "GUEST_USER" // Gunakan ID statis untuk tamu
                transaction.timestamp = System.currentTimeMillis()

                transactionDao.insertOrReplace(transaction)
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error inserting transaction for guest user", e)
            }
        }
    }

    suspend fun delete(transaction: Transaction) {
        val userId = currentUserId

        // Logika penghapusan untuk pengguna yang masuk dan tamu
        if (userId != null) {
            if (transaction.id.isEmpty()) return
            try {
                firestore.collection("users").document(userId)
                    .collection("transactions").document(transaction.id)
                    .delete().await()
                transactionDao.delete(transaction)
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error deleting transaction for logged-in user", e)
            }
        } else {
            // Mode Tamu: hanya hapus dari Room
            try {
                transactionDao.delete(transaction)
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error deleting transaction for guest user", e)
            }
        }
    }

    suspend fun clearAllTransactionsForUser() {
        val userId = currentUserId ?: "GUEST_USER" // Hapus data tamu jika tidak ada pengguna yang masuk

        transactionDao.deleteAllTransactionsForUser(userId)

        // Hanya hapus dari Firestore jika pengguna bukan tamu
        if (userId != "GUEST_USER") {
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
    }

    // --- Operasi MEMBACA (Read) ---

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

    fun startListeningForRemoteUpdates() {
        val userId = currentUserId
        if (userId == null) {
            return // Jangan dengarkan pembaruan Firestore untuk tamu
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

                            if (transaction.timestamp == 0L) {
                                transaction.timestamp = System.currentTimeMillis()
                            }

                            when (docChange.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    transactionDao.insertOrReplace(transaction)
                                }
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