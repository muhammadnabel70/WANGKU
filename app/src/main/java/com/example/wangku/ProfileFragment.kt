package com.example.wangku.ui.profile // Sesuaikan package Anda

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.wangku.LoginActivity
import com.example.wangku.R
import com.example.wangku.SettingsActivity // <-- IMPORT BARU
import com.example.wangku.WangKuApplication
import com.example.wangku.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder // <-- IMPORT MODERN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel TIDAK DIPERLUKAN LAGI di sini, kecuali untuk logout
    // (Tapi kita bisa panggil repo langsung untuk logout)
    // Mari kita biarkan untuk logout
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory((activity?.application as WangKuApplication).repository)
    }

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserInfo()
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnEditProfile.setOnClickListener {
            showEditNameDialog()
        }

        // [BERUBAH] Tombol Setting sekarang pindah ke SettingsActivity
        binding.btnSetting.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserInfo() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            binding.tvProfileName.text = user.displayName ?: "User"
            binding.tvProfileId.text = user.email
        } else {
            binding.tvProfileName.text = "Guest"
            binding.tvProfileId.text = ""
        }
    }

    private fun showEditNameDialog() {
        val user = firebaseAuth.currentUser ?: return

        // [PERBAIKAN UI] Gunakan MaterialAlertDialogBuilder
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Edit Name")

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null)
        builder.setView(dialogView)

        val nameEditText = dialogView.findViewById<EditText>(R.id.et_name)
        nameEditText.setText(user.displayName)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = nameEditText.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                updateProfileName(newName)
                dialog.dismiss()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun updateProfileName(newName: String) {
        // ... (Kode updateProfileName Anda tidak berubah)
    }

    /**
     * [PERBAIKAN UI] Dialog logout sekarang HANYA untuk logout
     * dan menggunakan style MODERN.
     */
    private fun showLogoutConfirmationDialog() {
        // [PERBAIKAN UI] Gunakan MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    // [DIHAPUS] Fungsi showDeleteDataConfirmationDialog()
    // sudah dipindah ke SettingsActivity.kt

    /**
     * [PERBAIKAN BUG] Fungsi ini HANYA logout, tidak menghapus data.
     */
    private fun performLogout() {
        // 1. Hapus sesi login Firebase
        firebaseAuth.signOut()

        // 2. Hapus sesi login Google (WAJIB agar bisa ganti akun)
        GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()

        // 3. Pindah kembali ke LoginActivity
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // 4. Tutup MainActivity
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}