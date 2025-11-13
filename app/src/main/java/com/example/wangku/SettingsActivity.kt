package com.example.wangku // Sesuaikan package Anda

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.wangku.databinding.ActivitySettingsBinding
import com.example.wangku.ui.profile.ProfileViewModel
import com.example.wangku.ui.profile.ProfileViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder // <-- IMPORT MODERN

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup ViewModel
        val app = application as WangKuApplication
        profileViewModel = ViewModelProvider(this, ProfileViewModelFactory(app.repository))
            .get(ProfileViewModel::class.java)

        // 2. Setup Toolbar
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tampilkan tombol back

        // 3. Setup Click Listener
        binding.btnDeleteData.setOnClickListener {
            showDeleteDataConfirmationDialog()
        }

        binding.btnChangeLanguage.setOnClickListener {
            Toast.makeText(this, "Fitur Ganti Bahasa segera hadir!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * [BARU] Menampilkan dialog Hapus Data yang MODERN
     */
    private fun showDeleteDataConfirmationDialog() {
        // [PERBAIKAN UI] Gunakan MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Semua Data")
            .setMessage("Apakah Anda yakin? Semua data transaksi (Income & Expense) Anda akan dihapus permanen dari HP ini dan dari cloud. Aksi ini tidak bisa dibatalkan.")
            .setPositiveButton("Hapus Data") { dialog, _ ->
                // Panggil ViewModel untuk menghapus data
                profileViewModel.clearAllUserData()
                Toast.makeText(this, "Semua data telah dihapus.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    /**
     * Menangani klik tombol "Back" di toolbar
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}