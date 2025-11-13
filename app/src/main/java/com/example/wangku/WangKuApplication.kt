package com.example.wangku // Sesuaikan package Anda

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Application class sekarang menjadi "rumah" bagi SEMUA singleton
 * yang berhubungan dengan data: Room, Firestore, Auth, dan Repository.
 */
class WangKuApplication : Application() {

    // 'by lazy' berarti objek hanya akan dibuat SATU KALI
    // saat pertama kali diakses.

    // 1. Singleton untuk Database Room (Lokal)
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    // 2. [BARU] Singleton untuk Firebase Auth
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // 3. [BARU] Singleton untuk Firebase Firestore
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // 4. [BERUBAH] Singleton untuk Repository
    // Kita sekarang memasukkan semua (Room, Firestore, Auth) ke dalamnya.
    val repository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao(), firestore, firebaseAuth)
    }
}