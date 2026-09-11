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
                val db = MamaFitDatabase.getDatabase(this@RegisterActivity)
                val existingUser = db.userDao().getUserByUsername(username)

                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "Username sudah terdaftar, silakan login", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    // Simpan user baru
                    val newUser = Pengguna(
                        username = username,
                        idPengguna = UUID.randomUUID().toString(),
                        nama = name,
                        noHp = noHp,
                        email = email,
                        password = password,
                        tanggalDaftar = Date()
                    )
                    db.userDao().insertUser(newUser)
                    
                    // Simpan session sementara agar OtpActivity tahu siapa yang sedang daftar
                    val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("temp_username", username)
                        apply()
                    }

                    Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, OtpActivity::class.java))
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