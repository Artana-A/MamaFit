package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<android.view.View>(R.id.btnLogin).setOnClickListener {
            performLogin()
        }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun performLogin() {
        val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi Username dan Password", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val supabase = SupabaseManager.client
                
                // Login via Email (menggunakan field username sebagai email untuk demo ini, 
                // atau Bunda bisa sesuaikan agar inputnya email)
                // Asumsi: field etUsername di layout diisi Email
                supabase.auth.signInWith(Email) {
                    this.email = username
                    this.password = password
                }

                val authUser = supabase.auth.currentUserOrNull()
                if (authUser != null) {
                    // Ambil detail profil dari tabel pengguna
                    val profile = supabase.postgrest["pengguna"]
                        .select {
                            filter {
                                eq("id_pengguna", authUser.id)
                            }
                        }.decodeSingle<Pengguna>()

                    // Simpan session
                    val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("user_id", profile.idPengguna)
                        putString("user_name", profile.namaLengkap)
                        putString("user_username", profile.namaPengguna)
                        putBoolean("is_logged_in", true)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "Selamat datang, ${profile.namaLengkap}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finishAffinity()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
