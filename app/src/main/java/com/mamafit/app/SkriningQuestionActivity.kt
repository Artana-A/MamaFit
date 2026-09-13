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
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SkriningQuestionActivity : AppCompatActivity() {

    private var questionIndex = 0
    private var selectedOptionIds = mutableSetOf<String>()
    private lateinit var filteredQuestions: List<SkriningQuestion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skrining_soal)

        val typeStr = intent.getStringExtra("EXTRA_TYPE") ?: "AWAL"
        val type = SkriningType.valueOf(typeStr)
        
        questionIndex = intent.getIntExtra(EXTRA_INDEX, 0)
        
        if (questionIndex == 0) {
            SkriningRepository.SkriningSession.reset()
            SkriningRepository.SkriningSession.type = type
        }
        
        filteredQuestions = getFilteredQuestions()
        renderQuestion()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun getFilteredQuestions(): List<SkriningQuestion> {
        val type = SkriningRepository.SkriningSession.type
        return if (type == SkriningType.AWAL) {
            SkriningData.questions
        } else {
            SkriningData.questions.filter { 
                it.section == SkriningSection.KONTRAINDIKASI_ABSOLUT || 
                it.section == SkriningSection.KONDISI_HARI_INI 
            }
        }
    }

    private fun renderQuestion() {
        if (questionIndex >= filteredQuestions.size) return
        
        val question = filteredQuestions[questionIndex]
        val totalQuestions = filteredQuestions.size
        val currentStep = questionIndex + 1

        selectedOptionIds.clear()
        // Restore previous answer if any (e.g. on back)
        val savedAnswer = SkriningRepository.SkriningSession.ambilJawaban(question.id)
        if (!savedAnswer.isNullOrEmpty()) {
            selectedOptionIds.addAll(savedAnswer.split(";"))
        }

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

        // Note: Follow-up questions (like Q7 or Q12) can be implemented by adding 
        // extra views to the layout and toggling them here. 
        // For now, we handle the logic in the repository.

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
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvOptionTitle)
            val tvSubtitle = itemView.findViewById<TextView>(R.id.tvOptionSubtitle)
            val radio = itemView.findViewById<View>(R.id.ivRadio)

            tvTitle.text = option.title
            ivIcon.setImageResource(option.iconRes)
            tvSubtitle.text = option.subtitle
            tvSubtitle.visibility = if (option.subtitle != null) View.VISIBLE else View.GONE

            val isSelected = selectedOptionIds.contains(option.id)
            cardBg.setBackgroundResource(if (isSelected) R.drawable.bg_option_card_selected else R.drawable.bg_option_card_normal)
            radio.setBackgroundResource(if (isSelected) R.drawable.bg_radio_on else R.drawable.bg_radio_off)

            itemView.setOnClickListener {
                if (question.isMultiSelect) {
                    if (isSelected) selectedOptionIds.remove(option.id)
                    else {
                        // If "tidak_ada" is selected, remove others. If others selected, remove "tidak_ada".
                        if (option.id == "tidak_ada") {
                            selectedOptionIds.clear()
                            selectedOptionIds.add("tidak_ada")
                        } else {
                            selectedOptionIds.remove("tidak_ada")
                            selectedOptionIds.add(option.id)
                        }
                    }
                } else {
                    selectedOptionIds.clear()
                    selectedOptionIds.add(option.id)
                }
                
                SkriningRepository.SkriningSession.simpanJawaban(question.id, selectedOptionIds.joinToString(";"))
                renderOptions(question)
                updateNextButtonState()
            }
            container.addView(itemView)
        }
    }

    private fun updateNextButtonState() {
        val btnNext = findViewById<View>(R.id.btnNext)
        val enabled = selectedOptionIds.isNotEmpty()
        btnNext.isEnabled = enabled
        btnNext.setBackgroundResource(if (enabled) R.drawable.bg_button_primary else R.drawable.bg_button_disabled)
        btnNext.setOnClickListener {
            if (enabled) goToNextQuestion()
        }
    }

    private fun goToNextQuestion() {
        val nextIndex = questionIndex + 1
        if (nextIndex < filteredQuestions.size) {
            val intent = Intent(this, SkriningQuestionActivity::class.java)
            intent.putExtra(EXTRA_INDEX, nextIndex)
            intent.putExtra("EXTRA_TYPE", SkriningRepository.SkriningSession.type.name)
            startActivity(intent)
        } else {
            lifecycleScope.launch {
                try {
                    val db = MamaFitDatabase.getDatabase(this@SkriningQuestionActivity)
                    val repository = SkriningRepository(db.hasilSkriningDao())
                    
                    val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
                    val userId = prefs.getString("user_id", "00000000-0000-0000-0000-000000000000") ?: "00000000-0000-0000-0000-000000000000"
                    
                    // Ambil jawaban SEBELUM simpan agar tidak ter-reset
                    val answers = SkriningRepository.SkriningSession.ambilSemuaJawaban()
                    val trimester = when(answers["q1"]) {
                        "t1" -> "1"
                        "t2" -> "2"
                        else -> "3"
                    }
                    
                    val level = repository.simpanHasilSkrining(userId)

                    // Simpan trimester ke prefs sebagai fallback cepat
                    prefs.edit().putString("user_trimester", trimester).apply()
                    
                    if (level == LevelRisiko.TINGGI) {
                        showHighRiskDialog()
                    } else {
                        val intent = Intent(this@SkriningQuestionActivity, HasilSkriningActivity::class.java)
                        intent.putExtra("EXTRA_LEVEL_RISIKO", level.name)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SkriningQuestionActivity, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showHighRiskDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Perhatian Khusus")
            .setMessage("Berdasarkan hasil skrining, kondisi Anda memerlukan perhatian medis segera. Hubungi tenaga medis sebelum berolahraga.")
            .setPositiveButton("Hubungi Bidan") { _, _ -> finish() }
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
