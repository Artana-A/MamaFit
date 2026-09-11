package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup

class HasilSkriningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_skrining)

        val riskLevelName = intent.getStringExtra("EXTRA_LEVEL_RISIKO") ?: LevelRisiko.RENDAH.name
        val level = LevelRisiko.valueOf(riskLevelName)

        updateUIBasedOnRisk(level)
        setupStatCards(level)
        setupNavTabs()

        findViewById<View>(R.id.btnDownloadPdf).setOnClickListener {
            Toast.makeText(this, "Unduh PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIBasedOnRisk(level: LevelRisiko) {
        val tvRiskLevel = findViewById<TextView>(R.id.tvRiskLevel)
        val statusPill = findViewById<View>(R.id.statusPill)
        val statusDot = findViewById<View>(R.id.statusDot)
        val tvStatusText = findViewById<TextView>(R.id.tvStatusText)
        val tvStatusMessage = findViewById<TextView>(R.id.tvStatusMessage)
        val tvRecommendationText = findViewById<TextView>(R.id.tvRecommendationText)
        val tvIntensityText = findViewById<TextView>(R.id.tvIntensityText)
        val ivResultIcon = findViewById<ImageView>(R.id.ivResultIcon)
        val btnDashboard = findViewById<View>(R.id.btnDashboard)
        val bottomNav = findViewById<View>(R.id.bottomNav)

        when (level) {
            LevelRisiko.TINGGI -> {
                tvRiskLevel.text = "Resiko Tinggi"
                tvRiskLevel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                
                statusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEB"))
                statusDot.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                tvStatusText.text = "Status: Merah"
                tvStatusText.setTextColor(Color.parseColor("#D32F2F"))
                
                tvStatusMessage.text = "Perlu Perhatian Medis!"
                tvRecommendationText.text = "\"Berdasarkan hasil skrining, Bunda sangat disarankan untuk beristirahat dan segera berkonsultasi dengan dokter atau bidan.\""
                tvIntensityText.text = "Intensitas: Sangat Ringan / Istirahat"
                ivResultIcon.setImageResource(R.drawable.ic_warning)
                ivResultIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)

                // Hapus akses ke Dashboard & Navigasi
                btnDashboard.setBackgroundResource(R.drawable.bg_button_dark)
                
                // Cari TextView di dalam btnDashboard untuk ganti teksnya
                findTextView(btnDashboard)?.text = "Hubungi Bidan"
                
                btnDashboard.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL)
                    startActivity(intent)
                }
                
                bottomNav.visibility = View.GONE
            }
            LevelRisiko.SEDANG -> {
                tvRiskLevel.text = "Resiko Sedang"
                tvRiskLevel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FBC02D"))
                
                statusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF9C4"))
                statusDot.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FBC02D"))
                tvStatusText.text = "Status: Kuning"
                tvStatusText.setTextColor(Color.parseColor("#FBC02D"))
                
                tvStatusMessage.text = "Kondisi Perlu Dipantau"
                tvRecommendationText.text = "\"Berdasarkan hasil skrining, Bunda diperbolehkan melakukan aktivitas ringan. Hindari gerakan yang terlalu membebani tubuh.\""
                tvIntensityText.text = "Intensitas: Ringan"
                ivResultIcon.setImageResource(R.drawable.ic_info)
                ivResultIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                
                btnDashboard.setOnClickListener {
                    navigateToDashboard()
                }
            }
            LevelRisiko.RENDAH -> {
                tvRiskLevel.text = "Resiko Rendah"
                btnDashboard.setOnClickListener {
                    navigateToDashboard()
                }
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun findTextView(view: View): TextView? {
        if (view is TextView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findTextView(child)
                if (result != null) return result
            }
        }
        return null
    }

    private fun setupStatCards(level: LevelRisiko) {
        lifecycleScope.launch {
            val db = MamaFitDatabase.getDatabase(this@HasilSkriningActivity)
            val latest = db.hasilSkriningDao().ambilSemuaSkrining("USER_123").firstOrNull()
            
            if (latest != null) {
                fillStat(R.id.statHeartRate, R.drawable.ic_monitor_heart, "Detak Jantung", "Normal")
                fillStat(R.id.statTemp, R.drawable.ic_thermometer, "Suhu Tubuh", "36.5°C")
                
                val td = when(latest.gejalaSaatIni.tekananDarah) {
                    TekananDarah.RENDAH -> "Rendah"
                    TekananDarah.TINGGI -> "Tinggi"
                    else -> "Normal"
                }
                fillStat(R.id.statBloodPressure, R.drawable.ic_favorite, "Tekanan Darah", td)
                
                val kesiapan = if (latest.gejalaSaatIni.bertenaga) "Optimal" else "Lelah"
                fillStat(R.id.statReadiness, R.drawable.ic_person, "Kesiapan", kesiapan)
            } else {
                fillStat(R.id.statHeartRate, R.drawable.ic_monitor_heart, "Detak Jantung", "Normal")
                fillStat(R.id.statTemp, R.drawable.ic_thermometer, "Suhu Tubuh", "36.5°C")
                fillStat(R.id.statBloodPressure, R.drawable.ic_favorite, "Tekanan Darah", "Normal")
                fillStat(R.id.statReadiness, R.drawable.ic_person, "Kesiapan", "Optimal")
            }
        }
    }

    private fun fillStat(containerId: Int, iconRes: Int, label: String, value: String) {
        val container = findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.ivStatIcon).setImageResource(iconRes)
        container.findViewById<TextView>(R.id.tvStatLabel).text = label
        container.findViewById<TextView>(R.id.tvStatValue).text = value
    }

    private fun setupNavTabs() {
        setupTab(R.id.navHome, R.drawable.ic_home, "Home")
        setupTab(R.id.navExercise, R.drawable.ic_exercise, "Exercise")
        setupTab(R.id.navJournal, R.drawable.ic_journal, "Journal")
        setupTab(R.id.navProfile, R.drawable.ic_person, "Profile")
    }

    private fun setupTab(containerId: Int, iconRes: Int, label: String) {
        val container = findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.ivNavIcon).setImageResource(iconRes)
        container.findViewById<TextView>(R.id.tvNavLabel).text = label
        container.setOnClickListener {
            Toast.makeText(this, "$label diklik", Toast.LENGTH_SHORT).show()
        }
    }
}
