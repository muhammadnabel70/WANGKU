package com.example.wangku // Sesuaikan package Anda

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wangku.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inisialisasi Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // 2. Tombol "Sign Up" diklik
        binding.btnSignup.setOnClickListener {
            performSignUp()
        }

        // 3. Tombol "Log In" diklik
        binding.btnLogin.setOnClickListener {
            // Kembali ke LoginActivity
            finish() // Tutup activity ini
        }
    }

    /**
     * Mengambil data dari field, memvalidasi, dan mendaftarkan
     * pengguna baru ke Firebase Authentication.
     */
    private fun performSignUp() {
        // 1. Ambil semua data input
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // 2. Validasi Input
        if (name.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
            binding.etName.requestFocus() // Fokus ke field nama
            return
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            binding.etEmail.requestFocus()
            return
        }
        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // 3. Tampilkan ProgressBar, sembunyikan tombol
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignup.visibility = View.GONE

        // 4. [KODE FIREBASE] Buat pengguna baru dengan email dan password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // Sign up berhasil, SEKARANG simpan nama
                    val user = firebaseAuth.currentUser

                    // 5. Buat request untuk update profil dengan nama
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build() // Anda juga bisa menambahkan .setPhotoUri() di sini

                    // 6. Update profil pengguna
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            // 7. Sembunyikan progress bar setelah SEMUA selesai
                            binding.progressBar.visibility = View.GONE
                            binding.btnSignup.visibility = View.VISIBLE

                            if (profileTask.isSuccessful) {
                                // Update profil berhasil!
                                Toast.makeText(this, "Sign up successful! Please log in.", Toast.LENGTH_LONG).show()
                                finish() // Kembali ke Login
                            } else {
                                // Gagal update profil (tapi akun sudah terbuat)
                                Toast.makeText(this, "Sign up successful, but failed to save name.", Toast.LENGTH_LONG).show()
                                finish() // Kembali ke Login
                            }
                        }

                } else {
                    // Sign up gagal (misal: email sudah terdaftar)
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignup.visibility = View.VISIBLE
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}