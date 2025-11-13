package com.example.wangku // Sesuaikan package Anda

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.wangku.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // Deklarasi untuk Google Sign In
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Cek apakah pengguna sudah login sebelumnya (sesi masih ada)
        if (firebaseAuth.currentUser != null) {
            goToMainActivity()
        }

        // 1. Konfigurasi Google Sign In
        configureGoogleSignIn()

        // 2. Inisialisasi Activity Result Launcher (pengganti onActivityResult)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Callback saat Google Sign-In selesai
            onGoogleLoginResult(result)
        }

        // --- Click Listeners ---

        // 3. Tombol Login (Email/Password) diklik
        binding.btnLogin.setOnClickListener {
            performEmailLogin()
        }

        // 4. Tombol Sign Up diklik
        binding.btnSignup.setOnClickListener {
            // Pindah ke SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 5. Tombol Guest diklik
        binding.btnGuest.setOnClickListener {
            Toast.makeText(this, "Login sebagai Guest", Toast.LENGTH_SHORT).show()
            goToMainActivity()
        }

        // 6. Tombol Google diklik
        binding.btnGoogleLogin.setOnClickListener {
            performGoogleLogin()
        }

        // 7. Tombol Forgot Password diklik (placeholder)
        binding.tvForgotPassword.setOnClickListener {
            // TODO: Buat logic untuk reset password
            Toast.makeText(this, "Forgot Password clicked (Not Implemented)", Toast.LENGTH_SHORT).show()
        }
    }

    // --- [LOGIKA GOOGLE SIGN-IN] ---

    /**
     * Menyiapkan GoogleSignInOptions
     */
    private fun configureGoogleSignIn() {
        // Konfigurasi Google Sign In untuk meminta ID Token
        // 'default_web_client_id' diambil OTOMATIS dari file google-services.json
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Memulai alur login Google
     */
    private fun performGoogleLogin() {
        showLoading(true)
        // Logout dulu untuk memastikan pemilihan akun muncul
        googleSignInClient.signOut().addOnCompleteListener {
            // Luncurkan intent login Google
            val signInIntent = googleSignInClient.signInIntent
            activityResultLauncher.launch(signInIntent)
        }
    }

    /**
     * Menangani hasil dari alur login Google
     */
    private fun onGoogleLoginResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // Dapatkan akun Google dari intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In berhasil, sekarang autentikasi dengan Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In gagal
                showLoading(false)
                Toast.makeText(this, "Google Sign In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Alur Google dibatalkan oleh pengguna
            showLoading(false)
        }
    }

    /**
     * Mengautentikasi akun Google ke Firebase
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login Firebase berhasil
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    // Login Firebase gagal
                    showLoading(false)
                    Toast.makeText(this, "Firebase Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- [LOGIKA EMAIL/PASSWORD LOGIN] ---

    private fun performEmailLogin() {
        val email = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validasi
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etUsername.error = "Enter a valid email"
            binding.etUsername.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
            binding.etPassword.requestFocus()
            return
        }

        showLoading(true)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- [FUNGSI HELPER] ---

    /**
     * Menampilkan/menyembunyikan ProgressBar dan menonaktifkan tombol
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.GONE
            binding.btnGoogleLogin.visibility = View.GONE
            binding.btnSignup.visibility = View.GONE
            binding.btnGuest.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnGoogleLogin.visibility = View.VISIBLE
            binding.btnSignup.visibility = View.VISIBLE
            binding.btnGuest.visibility = View.VISIBLE
        }
    }

    /**
     * Fungsi helper untuk pindah ke MainActivity
     * dan menutup LoginActivity.
     */
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
