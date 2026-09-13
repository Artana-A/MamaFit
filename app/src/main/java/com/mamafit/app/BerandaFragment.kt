package com.mamafit.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class BerandaFragment : Fragment(R.layout.fragment_beranda) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting(view)
        loadDatabaseStats(view)

        view.findViewById<View>(R.id.btnMulaiSkrining).setOnClickListener {
            val intent = Intent(requireContext(), SkriningQuestionActivity::class.java)
            intent.putExtra(SkriningQuestionActivity.EXTRA_INDEX, 0)
            intent.putExtra("EXTRA_TYPE", SkriningType.BULANAN.name)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btnMulaiLatihan).setOnClickListener {
            // Berpindah ke tab Olahraga di DashboardActivity
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav?.selectedItemId = R.id.nav_olahraga
        }

        view.findViewById<View>(R.id.streakCard).setOnClickListener {
            // TODO: buka halaman detail streak
        }
    }

    private fun setupGreeting(view: View) {
        val prefs = requireContext().getSharedPreferences("mamafit_prefs", Context.MODE_PRIVATE)
        val fullName = prefs.getString("user_name", "Mama")
        val firstName = fullName?.split(" ")?.firstOrNull() ?: "Mama"
        
        view.findViewById<TextView>(R.id.tvGreeting).text = "Halo, $firstName!"
        
        // Set default text agar tidak kosong saat loading
        val trimester = prefs.getString("user_trimester", "1")
        view.findViewById<TextView>(R.id.tvGreetingSub).text = "Trimester $trimester • Memuat data..."
    }

    private fun loadDatabaseStats(view: View) {
        val prefs = requireContext().getSharedPreferences("mamafit_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", "00000000-0000-0000-0000-000000000000") ?: "00000000-0000-0000-0000-000000000000"

        lifecycleScope.launch {
            val db = MamaFitDatabase.getDatabase(requireContext())
            
            // 1. Ambil Trimester & Risiko Terbaru
            val allSkrining = db.hasilSkriningDao().ambilSemuaSkrining(userId)
            val latestSkrining = allSkrining.firstOrNull()
            val tvSub = view.findViewById<TextView>(R.id.tvGreetingSub)
            
            android.util.Log.d("MamaFit", "User ID: $userId, Skrining Count: ${allSkrining.size}")

            if (latestSkrining != null) {
                val tri = when(latestSkrining.trimester) {
                    Trimester.SATU -> "1"
                    Trimester.DUA -> "2"
                    Trimester.TIGA -> "3"
                }
                // Hitung estimasi minggu berdasarkan trimester saat skrining
                val startWeek = when(latestSkrining.trimester) {
                    Trimester.SATU -> 4
                    Trimester.DUA -> 13
                    Trimester.TIGA -> 27
                }
                val diffMillis = System.currentTimeMillis() - latestSkrining.tanggalPengisian.time
                val weeksSinceLastSkrining = (diffMillis / (1000 * 60 * 60 * 24 * 7)).toInt()
                val estimatedWeek = startWeek + weeksSinceLastSkrining

                tvSub.text = "Trimester $tri • Minggu ke $estimatedWeek"
                tvSub.setTextColor(ContextCompat.getColor(requireContext(), R.color.pink_primary))
                tvSub.visibility = View.VISIBLE
                
                // Update SharedPreferences agar fragmen lain (Profil) sinkron
                prefs.edit().putString("user_trimester", tri).apply()
            } else {
                // Gunakan data dari prefs jika database belum sinkron
                val tri = prefs.getString("user_trimester", "1")
                tvSub.text = "Trimester $tri • Belum ada riwayat mingguan"
                tvSub.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
                tvSub.visibility = View.VISIBLE
            }

            // 2. Hitung Statistik
            val cal = Calendar.getInstance()
            
            // a. Today's stats for Progress Card
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val sinceToday = cal.timeInMillis
            
            val todayMenit = db.sesiLatihanDao().getTotalDurasiSince(userId, sinceToday) ?: 0
            val targetHarian = 30 // Target default 30 menit
            val progressPercent = (todayMenit * 100 / targetHarian).coerceAtMost(100)
            
            view.findViewById<TextView>(R.id.tvTargetHarian).text = "$todayMenit / $targetHarian mnt"
            view.findViewById<CircularProgressIndicator>(R.id.progressTarget).progress = progressPercent
            view.findViewById<TextView>(R.id.tvProgressPercent).text = "$progressPercent%"
            
            val remaining = (targetHarian - todayMenit).coerceAtLeast(0)
            val tvProgressDesc = view.findViewById<TextView>(R.id.tvProgressDesc)
            if (remaining > 0) {
                tvProgressDesc.text = "$remaining menit lagi untuk mencapai target hari ini."
            } else {
                tvProgressDesc.text = "Selamat! Bunda sudah mencapai target hari ini 🧡"
            }

            // b. Weekly summary
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val sinceWeek = cal.timeInMillis

            val totalMenitWeek = db.sesiLatihanDao().getTotalDurasiSince(userId, sinceWeek) ?: 0
            val totalKaloriWeek = db.sesiLatihanDao().getTotalKaloriSince(userId, sinceWeek) ?: 0f
            
            view.findViewById<TextView>(R.id.tvTotalMenit).text = totalMenitWeek.toString()
            view.findViewById<TextView>(R.id.tvTotalKalori).text = String.format(Locale.getDefault(), "%,.0f", totalKaloriWeek)
            
            // Update Streak
            val allSesi = db.sesiLatihanDao().ambilRiwayatSesi(userId)
            val streakCount = calculateStreak(allSesi)
            view.findViewById<TextView>(R.id.tvStreakCount).text = "Streak: $streakCount Hari"
        }
    }

    private fun calculateStreak(sesi: List<SesiLatihanEntity>): Int {
        if (sesi.isEmpty()) return 0
        // Logika sederhana hitung streak hari berturut-turut dari data asli
        val days = sesi.map { 
            val c = Calendar.getInstance()
            c.time = it.waktuSelesai
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            c.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        val current = Calendar.getInstance()
        current.set(Calendar.HOUR_OF_DAY, 0)
        current.set(Calendar.MINUTE, 0)
        current.set(Calendar.SECOND, 0)
        current.set(Calendar.MILLISECOND, 0)
        
        var checkDay = current.timeInMillis

        for (day in days) {
            if (day == checkDay) {
                streak++
                checkDay -= 24 * 60 * 60 * 1000
            } else if (day < checkDay) {
                break
            }
        }
        return streak
    }
}
