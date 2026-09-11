package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OtpActivity : AppCompatActivity() {

    private lateinit var otpBoxes: List<EditText>
    private var currentIndex = 0
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        otpBoxes = listOf(
            findViewById(R.id.etOtp1), findViewById(R.id.etOtp2),
            findViewById(R.id.etOtp3), findViewById(R.id.etOtp4),
            findViewById(R.id.etOtp5), findViewById(R.id.etOtp6)
        )

        // Nonaktifkan keyboard bawaan, biar input cuma lewat keypad custom
        otpBoxes.forEach { it.isFocusable = false; it.isCursorVisible = false }

        setupBackButton()
        setupKeypad()
        setupResendText()
        startCountdown()
        setupVerifyButton()
    }

    private fun setupBackButton() {
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupKeypad() {
        val keyIds = listOf(
            R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
            R.id.key6, R.id.key7, R.id.key8, R.id.key9
        )
        keyIds.forEachIndexed { i, id ->
            findViewById<TextView>(id).setOnClickListener { onDigitEntered((i + 1).toString()) }
        }
        findViewById<TextView>(R.id.key0).setOnClickListener { onDigitEntered("0") }
        findViewById<android.view.View>(R.id.keyBackspace).setOnClickListener { onBackspace() }
    }

    private fun onDigitEntered(digit: String) {
        if (currentIndex >= otpBoxes.size) return

        otpBoxes[currentIndex].setText(digit)
        otpBoxes[currentIndex].setBackgroundResource(R.drawable.bg_otp_box_empty)
        currentIndex++

        if (currentIndex < otpBoxes.size) {
            otpBoxes[currentIndex].setBackgroundResource(R.drawable.bg_otp_box_focus)
        }
    }

    private fun onBackspace() {
        if (currentIndex <= 0) return

        currentIndex--
        otpBoxes[currentIndex].setText("")
        otpBoxes[currentIndex].setBackgroundResource(R.drawable.bg_otp_box_focus)

        if (currentIndex + 1 < otpBoxes.size) {
            otpBoxes[currentIndex + 1].setBackgroundResource(R.drawable.bg_otp_box_empty)
        }
    }

    private fun setupResendText() {
        findViewById<TextView>(R.id.tvResend).apply {
            text = getString(R.string.otp_resend_question) + getString(R.string.otp_resend_action)
        }
    }

    private fun startCountdown() {
        val tvCountdown = findViewById<TextView>(R.id.tvCountdown)
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(59_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                tvCountdown.text = String.format("Tunggu 00:%02d", seconds)
            }
            override fun onFinish() {
                tvCountdown.text = "Kode kedaluwarsa"
            }
        }.start()
    }

    private fun setupVerifyButton() {
        findViewById<android.view.View>(R.id.btnVerify).setOnClickListener {
            // Tandai sudah login
            val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", true).apply()

            // Langsung pindah ke SkriningActivity tanpa validasi 6 digit angka
            val intent = Intent(this, SkriningActivity::class.java)
            startActivity(intent)
            finish() // Menutup OtpActivity
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        countDownTimer = null
    }
}