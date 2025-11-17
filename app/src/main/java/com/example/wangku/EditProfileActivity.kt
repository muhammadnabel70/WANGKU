package com.example.wangku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.example.wangku.databinding.ActivityEditProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarEditProfile.setNavigationOnClickListener {
            finish()
        }

        binding.btnEditName.setOnClickListener {
            showEditNameDialog()
        }

        binding.btnChangeEmail.setOnClickListener {
            Toast.makeText(this, "Fitur Ganti Email segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditNameDialog() {
        val user = firebaseAuth.currentUser ?: return

        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Edit Name")

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name, null)
        builder.setView(dialogView)

        val nameEditText = dialogView.findViewById<EditText>(R.id.et_name)
        nameEditText.setText(user.displayName)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = nameEditText.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Profile name updated!", Toast.LENGTH_SHORT).show()
                    // TODO: Tambahkan cara untuk memberitahu ProfileFragment agar me-refresh nama
                } else {
                    Toast.makeText(this, "Failed to update name: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}