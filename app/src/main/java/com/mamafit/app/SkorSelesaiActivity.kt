package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mamafit.app.databinding.ActivitySkorSelesaiBinding
import kotlinx.coroutines.launch

class SkorSelesaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySkorSelesaiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkorSelesaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sesiId = intent.getStringExtra("EXTRA_SESI_ID") ?: ""

        loadSessionData(sesiId)

        binding.btnCatatMood.setOnClickListener {
            goToDashboard(DashboardActivity.TAB_JURNAL)
        }

        binding.btnSelesaiSesi.setOnClickListener {
            goToDashboard(DashboardActivity.TAB_BERANDA)
        }
    }

    private fun loadSessionData(id: String) {
        lifecycleScope.launch {
            val db = MamaFitDatabase.getDatabase(this@SkorSelesaiActivity)
            // Ambil data terbaru jika ID kosong, atau ambil berdasarkan ID
            val allSesi = db.sesiLatihanDao().ambilRiwayatSesi("USER_123")
            val sesi = allSesi.find { it.idSesi == id } ?: allSesi.firstOrNull()

            sesi?.let {
                binding.tvExerciseName.text = it.namaGerakan
                binding.tvScore.text = it.skorPostur.toString()
                binding.progressAkurasi.progress = it.skorPostur
                binding.tvDuration.text = "${it.durasiMenit} mnt"
                binding.tvCalories.text = "${it.kaloriTerbakar.toInt()} kkal"
                binding.tvAiFeedback.text = it.feedbackAi
                
                // Jika ingin menampilkan total reps, tambahkan kolom di entity jika diperlukan
                // binding.tvTotalReps.text = "10" 
            }
        }
    }

    private fun goToDashboard(tab: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra(DashboardActivity.EXTRA_START_TAB, tab)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
