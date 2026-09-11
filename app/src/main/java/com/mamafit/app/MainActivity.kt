package com.mamafit.app

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cek Session
        val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        setupLoginText()
        setupButtonClicks()
    }

    private fun setupLoginText() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val fullText = getString(R.string.already_have_account) + getString(R.string.login_here)
        val spannable = SpannableString(fullText)

        val startIndex = getString(R.string.already_have_account).length
        val endIndex = fullText.length

        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.pink_primary)),
            startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvLogin.text = spannable
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupButtonClicks() {
        findViewById<android.view.View>(R.id.btnStart).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnLoginPhone).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}