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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide // [BARU] Import Glide
import com.example.wangku.LoginActivity
import com.example.wangku.R
import com.example.wangku.SettingsActivity
import com.example.wangku.WangKuApplication
import com.example.wangku.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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

        binding.toolbarProfile.setNavigationOnClickListener {
            findNavController().navigateUp() // Kembali ke fragment sebelumnya
        }

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

        binding.btnSetting.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSecurity.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Security Segera Hadir", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Help Segera Hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserInfo() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            binding.tvProfileName.text = user.displayName ?: "User"
            binding.tvProfileId.text = user.email

            // [PERBAIKAN] Load foto profil jika URL tersedia
            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_profile) // Gambar default saat loading
                    .error(R.drawable.ic_profile)       // Gambar default jika gagal load
                    .into(binding.ivProfilePicture)
            }
        } else {
            binding.tvProfileName.text = "Guest"
            binding.tvProfileId.text = ""
        }
    }

    private fun showEditNameDialog() {
        val user = firebaseAuth.currentUser ?: return

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
        val user = firebaseAuth.currentUser ?: return
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile name updated!", Toast.LENGTH_SHORT).show()
                    binding.tvProfileName.text = newName
                } else {
                    Toast.makeText(requireContext(), "Failed to update name: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLogoutConfirmationDialog() {
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

    private fun performLogout() {
        firebaseAuth.signOut()
        GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()

        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}