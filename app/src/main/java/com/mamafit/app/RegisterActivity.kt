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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
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
                    val authUser = supabase.auth.currentUserOrNull()
                    if (authUser == null) {
                        Toast.makeText(this@RegisterActivity, "Gagal mendaftarkan akun", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // 3. Simpan profil tambahan ke tabel 'pengguna'
                    val newUser = Pengguna(
                        namaPengguna = username,
                        idPengguna = authUser.id,
                        namaLengkap = name,
                        nomorTelepon = noHp,
                        email = email,
                        kataSandi = password, // Tetap simpan lokal jika perlu, tapi Supabase Auth sudah handle
                        tanggalDaftar = Date()
                    )
                    
                    supabase.postgrest["pengguna"].insert(newUser)

                    // 4. Simpan lokal di Room sebagai cache
                    val db = MamaFitDatabase.getDatabase(this@RegisterActivity)
                    db.userDao().insertUser(newUser)
                    
                    val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("temp_username", username)
                        apply()
                    }

                    Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, OtpActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
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