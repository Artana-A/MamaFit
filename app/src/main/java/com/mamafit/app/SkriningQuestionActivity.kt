package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SkriningQuestionActivity : AppCompatActivity() {

    private var questionIndex = 0
    private var selectedOptionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skrining_soal)

        questionIndex = intent.getIntExtra(EXTRA_INDEX, 0)
        
        // Reset data jika baru mulai dari soal pertama
        if (questionIndex == 0) {
            SkriningRepository.SkriningSession.reset()
        }
        
        renderQuestion()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSaveDraft).setOnClickListener {
            // TODO: simpan progres ke local storage / server
        }
    }

    private fun renderQuestion() {
        val question = SkriningData.questions[questionIndex]
        val totalQuestions = SkriningData.questions.size
        val currentStep = questionIndex + 1

        selectedOptionId = null

        findViewById<TextView>(R.id.tvStepCount).text =
            "Halaman $currentStep dari $totalQuestions"
        findViewById<TextView>(R.id.tvSectionLabel).text = question.section.displayName
        findViewById<TextView>(R.id.tvPercent).text =
            "${(currentStep * 100 / totalQuestions)}%"
        findViewById<ProgressBar>(R.id.progressBar).progress =
            currentStep * 100 / totalQuestions

        findViewById<TextView>(R.id.tvQuestionTitle).text = question.title
        findViewById<TextView>(R.id.tvQuestionDesc).text = question.description
        findViewById<TextView>(R.id.tvNextLabel).text = question.nextButtonLabel

        // Catatan medis (opsional)
        val noteBox = findViewById<View>(R.id.noteBox)
        if (question.note != null) {
            noteBox.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvNoteTitle).text = question.noteTitle
            findViewById<TextView>(R.id.tvNoteBody).text = question.note
        } else {
            noteBox.visibility = View.GONE
        }

        renderOptions(question)
        updateNextButtonState()
    }

    private fun renderOptions(question: SkriningQuestion) {
        val container = findViewById<LinearLayout>(R.id.optionsContainer)
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        question.options.forEach { option ->
            val itemView = inflater.inflate(R.layout.item_skrining_option, container, false)

            val cardBg = itemView.findViewById<LinearLayout>(R.id.optionCard)
            val iconBg = itemView.findViewById<FrameLayout>(R.id.iconBg)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvOptionTitle)
            val tvBadge = itemView.findViewById<TextView>(R.id.tvBadge)
            val tvSubtitle = itemView.findViewById<TextView>(R.id.tvOptionSubtitle)
            val radio = itemView.findViewById<View>(R.id.ivRadio)

            tvTitle.text = option.title
            ivIcon.setImageResource(option.iconRes)

            if (option.subtitle != null) {
                tvSubtitle.text = option.subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }

            if (option.badge != null) {
                tvBadge.text = option.badge
                tvBadge.visibility = View.VISIBLE
            } else {
                tvBadge.visibility = View.GONE
            }

            iconBg.setBackgroundResource(
                if (option.squareIcon) R.drawable.bg_icon_square_light
                else R.drawable.bg_icon_circle_light
            )

            fun applySelectedStyle(isSelected: Boolean) {
                cardBg.setBackgroundResource(
                    if (isSelected) R.drawable.bg_option_card_selected
                    else R.drawable.bg_option_card_normal
                )
                radio.setBackgroundResource(
                    if (isSelected) R.drawable.bg_radio_on else R.drawable.bg_radio_off
                )
            }
            applySelectedStyle(option.id == selectedOptionId)

            itemView.setOnClickListener {
                selectedOptionId = option.id
                
                // Simpan ke sesi sementara
                SkriningRepository.SkriningSession.simpanJawaban(questionIndex, option.id)
                
                renderOptions(question)
                updateNextButtonState()
            }
            itemView.tag = option.id

            container.addView(itemView)
        }
    }

    private fun updateNextButtonState() {
        val btnNext = findViewById<View>(R.id.btnNext)
        val enabled = selectedOptionId != null
        btnNext.isEnabled = enabled
        btnNext.setBackgroundResource(
            if (enabled) R.drawable.bg_button_primary else R.drawable.bg_button_disabled
        )
        btnNext.setOnClickListener {
            if (!enabled) return@setOnClickListener
            goToNextQuestion()
        }
    }

    private fun goToNextQuestion() {
        val nextIndex = questionIndex + 1
        if (nextIndex < SkriningData.questions.size) {
            val intent = Intent(this, SkriningQuestionActivity::class.java)
            intent.putExtra(EXTRA_INDEX, nextIndex)
            startActivity(intent)
        } else {
            // SOAL TERAKHIR: Proses hasil dan simpan ke Database
            lifecycleScope.launch {
                try {
                    val db = MamaFitDatabase.getDatabase(this@SkriningQuestionActivity)
                    val repository = SkriningRepository(db.hasilSkriningDao())
                    
                    // Simpan dan dapatkan hasil risiko
                    val level = repository.simpanHasilSkrining("USER_123")
                    
                    // Simpan pilihan trimester ke SharedPreferences agar tersinkronisasi di Profil & Beranda
                    val jawabanMap = SkriningRepository.SkriningSession.ambilSemuaJawaban()
                    val trimesterVal = when(jawabanMap[0]) {
                        "t1" -> "1"
                        "t2" -> "2"
                        else -> "3"
                    }
                    val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
                    prefs.edit().putString("user_trimester", trimesterVal).apply()

                    when (level) {
                        LevelRisiko.TINGGI -> {
                            showHighRiskDialog()
                        }
                        else -> {
                            val intent = Intent(this@SkriningQuestionActivity, HasilSkriningActivity::class.java)
                            intent.putExtra("EXTRA_LEVEL_RISIKO", level.name)
                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MamaFit", "Database Error: ${e.message}")
                    Toast.makeText(this@SkriningQuestionActivity, "Gagal menyimpan hasil: ${e.message}", Toast.LENGTH_LONG).show()
                    
                    // Fallback: Tetap pindah halaman meskipun gagal simpan agar tidak freeze
                    val intent = Intent(this@SkriningQuestionActivity, HasilSkriningActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showHighRiskDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Perhatian Khusus")
            .setMessage("Berdasarkan hasil skrining, kondisi Anda memerlukan perhatian medis segera. Kami menyarankan Bunda untuk segera berkonsultasi dengan dokter atau bidan sebelum melakukan aktivitas fisik.")
            .setPositiveButton("Hubungi Bidan") { _, _ ->
                // Contoh: Buka WhatsApp atau Telepon
                val intent = Intent(Intent.ACTION_DIAL)
                // intent.data = Uri.parse("tel:08123456789") 
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Lihat Hasil") { _, _ ->
                val intent = Intent(this, HasilSkriningActivity::class.java)
                intent.putExtra("EXTRA_LEVEL_RISIKO", LevelRisiko.TINGGI.name)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        const val EXTRA_INDEX = "extra_index"
    }
}