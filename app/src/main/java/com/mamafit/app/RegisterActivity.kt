package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private fun setupTermsText() {
        val tvTerms = findViewById<TextView>(R.id.tvTerms)

        val prefix = getString(R.string.terms_prefix)
        val term1 = getString(R.string.terms_1)
        val and = getString(R.string.terms_and)
        val term2 = getString(R.string.terms_2)
        val suffix = getString(R.string.terms_suffix)

        val fullText = prefix + term1 + and + term2 + suffix
        val spannable = SpannableString(fullText)

        // Warnai "Ketentuan Layanan"
        var start = prefix.length
        var end = start + term1.length
        spannable.setSpan(ForegroundColorSpan(getColor(R.color.pink_primary)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Warnai "Kebijakan Privasi"
        start = end + and.length
        end = start + term2.length
        spannable.setSpan(ForegroundColorSpan(getColor(R.color.pink_primary)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvTerms.text = spannable
    }

    private fun setupContinueButton() {
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener {
            val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
            val noHp = findViewById<EditText>(R.id.etPhone).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

            if (name.isEmpty() || username.isEmpty() || noHp.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lengkapi semua data dulu ya", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val supabase = SupabaseManager.client
                    
                    // 1. Daftar ke Supabase Auth
                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    // 2. Ambil User ID dari Auth
                    // Jika "Confirm Email" ON di Supabase, authUser akan NULL karena belum ada session.
                    val authUser = supabase.auth.currentUserOrNull()
                    
                    if (authUser == null) {
                        // Cek apakah ini karena butuh konfirmasi email (fitur Supabase default)
                        Toast.makeText(this@RegisterActivity, 
                            "Silakan cek email Bunda untuk konfirmasi (atau matikan 'Confirm Email' di Supabase).", 
                            Toast.LENGTH_LONG).show()
                        
                        // ID sementara untuk testing
                        saveUserToDatabase(UUID.randomUUID().toString(), username, name, noHp, email, password)
                    } else {
                        saveUserToDatabase(authUser.id, username, name, noHp, email, password)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MamaFit", "Register Error: ${e.message}", e)
                    Toast.makeText(this@RegisterActivity, "Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun saveUserToDatabase(id: String, username: String, name: String, noHp: String, email: String, pass: String) {
        val newUser = Pengguna(
            namaPengguna = username,
            idPengguna = id,
            namaLengkap = name,
            nomorTelepon = noHp,
            email = email,
            kataSandi = pass,
            tanggalDaftar = Date()
        )
        
        // Simpan ke Supabase Postgrest (opsional fail-safe)
        try {
            val userData = buildJsonObject {
                put("id_pengguna", newUser.idPengguna)
                put("nama_pengguna", newUser.namaPengguna)
                put("nama_lengkap", newUser.namaLengkap)
                put("nomor_telepon", newUser.nomorTelepon)
                put("email", newUser.email)
                put("kata_sandi", newUser.kataSandi)
                put("tanggal_daftar", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US).format(newUser.tanggalDaftar))
            }
            SupabaseManager.client.postgrest["pengguna"].insert(userData)
        } catch (e: Exception) {
            android.util.Log.e("MamaFit", "Supabase DB Error: ${e.message}")
        }

        // Simpan lokal di Room
        val db = MamaFitDatabase.getDatabase(this@RegisterActivity)
        db.userDao().insertUser(newUser)
        
        getSharedPreferences("mamafit_prefs", MODE_PRIVATE).edit().apply {
            putString("user_id", id)
            putString("user_name", name)
            putString("user_username", username)
            putBoolean("is_logged_in", true)
            apply()
        }
        
        Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@RegisterActivity, OtpActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupKeyboardInsets()
        setupTermsText()
        setupContinueButton()
    }

    private fun setupKeyboardInsets() {
        val rootView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            // Ambil yang lebih besar: tinggi keyboard atau nav bar
            view.setPadding(0, 0, 0, maxOf(imeHeight, navBarHeight))
            insets
        }
    }
}