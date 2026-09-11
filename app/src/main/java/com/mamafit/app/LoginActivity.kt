package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
            val db = MamaFitDatabase.getDatabase(this@LoginActivity)
            val user = db.userDao().loginUser(username, password)

            if (user != null) {
                // Simpan session
                val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
                prefs.edit().apply {
                    putString("user_id", user.idPengguna)
                    putString("user_name", user.nama)
                    putString("user_username", user.username)
                    putBoolean("is_logged_in", true)
                    apply()
                }

                Toast.makeText(this@LoginActivity, "Selamat datang, ${user.nama}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                finishAffinity() // Tutup semua activity sebelumnya
            } else {
                Toast.makeText(this@LoginActivity, "Username atau Password salah", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
