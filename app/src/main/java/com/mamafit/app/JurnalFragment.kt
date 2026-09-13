package com.mamafit.app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class JurnalFragment : Fragment(R.layout.fragment_jurnal) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sembunyikan wordmark "MamaFit" di top bar (desain Jurnal cuma avatar + bell)
        view.findViewById<View>(R.id.tvTopBarLogo).visibility = View.GONE

        setupHariAktif(view)
        setupMood(view)
        setupKalender(view)
        loadJournalData(view)

        view.findViewById<View>(R.id.btnKirimSuami).setOnClickListener {
            // TODO: kirim laporan ke suami
        }
    }

    private fun setupHariAktif(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.hariAktifBars)
        val hariLabel = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val aktif = listOf(true, true, true, true, true, true, false)

        hariLabel.forEachIndexed { i, label ->
            val bar = View(requireContext())
            val params = LinearLayout.LayoutParams(0, dpToPx(6)).apply {
                weight = 1f
                marginEnd = if (i != hariLabel.lastIndex) dpToPx(4) else 0
            }
            bar.layoutParams = params
            bar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (aktif[i]) R.color.pink_primary else R.color.progress_track
                )
            )
            container.addView(bar)
        }
    }

    private fun setupMood(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.moodContainer)
        val moods = listOf("😔", "😐", "🙂", "😊", "🤩")
        
        val prefs = requireContext().getSharedPreferences("mamafit_prefs", android.content.Context.MODE_PRIVATE)
        var selectedIndex = prefs.getInt("daily_mood_index", 3)

        fun render() {
            container.removeAllViews()
            moods.forEachIndexed { i, emoji ->
                val itemView = layoutInflater.inflate(R.layout.item_mood, container, false)
                val tv = itemView.findViewById<TextView>(R.id.tvMoodEmoji)
                tv.text = emoji
                tv.setBackgroundResource(
                    if (i == selectedIndex) R.drawable.bg_avatar_circle else R.drawable.bg_icon_circle_light
                )
                // Set listener pada TV langsung agar klik terdeteksi
                tv.setOnClickListener {
                    selectedIndex = i
                    prefs.edit().putInt("daily_mood_index", i).apply()
                    render()
                }
                container.addView(itemView)
            }
        }
        render()
    }

    private fun setupKalender(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.calendarContainer)
        val hariLabel = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        
        val cal = Calendar.getInstance()
        val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        
        // Set ke hari Senin di minggu ini
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            // Jika hari ini Minggu, mundur 6 hari agar tetap di minggu yang sama (tergantung Locale)
            cal.add(Calendar.DAY_OF_YEAR, -6)
        }
        
        container.removeAllViews()

        for (i in 0 until 7) {
            val date = cal.get(Calendar.DAY_OF_MONTH)
            val isToday = cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
            
            val itemView = layoutInflater.inflate(R.layout.item_calendar_day, container, false)
            itemView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
            
            itemView.findViewById<TextView>(R.id.tvDayLabel).text = hariLabel[i]
            val tvDate = itemView.findViewById<TextView>(R.id.tvDayDate)
            tvDate.text = date.toString()
            
            if (isToday) {
                tvDate.setBackgroundResource(R.drawable.bg_avatar_circle)
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                tvDate.setBackgroundResource(0)
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
            }
            
            container.addView(itemView)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun loadJournalData(view: View) {
        val prefs = requireContext().getSharedPreferences("mamafit_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", "00000000-0000-0000-0000-000000000000") ?: "00000000-0000-0000-0000-000000000000"

        lifecycleScope.launch {
            val db = MamaFitDatabase.getDatabase(requireContext())
            
            // 1. Tentukan Range Minggu Ini
            val cal = Calendar.getInstance()
            val df = SimpleDateFormat("dd MMM", Locale("id", "ID"))
            
            // Set ke Senin minggu ini
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startStr = df.format(cal.time)
            
            // Set ke Minggu minggu ini
            cal.add(Calendar.DAY_OF_YEAR, 6)
            val endStr = df.format(cal.time)
            
            val year = Calendar.getInstance().get(Calendar.YEAR)
            view.findViewById<TextView>(R.id.tvWeeklyRange).text = String.format(Locale("id", "ID"), "%s – %s %d", startStr, endStr, year)

            // 2. Load Stats
            // Reset ke Senin
            cal.add(Calendar.DAY_OF_YEAR, -6)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val since = cal.timeInMillis
            val totalMenit = db.sesiLatihanDao().getTotalDurasiSince(userId, since) ?: 0
            val countHari = db.sesiLatihanDao().getCountHariAktifSince(userId, since)
            
            view.findViewById<TextView>(R.id.tvTotalMenitJurnal).text = String.format(Locale.getDefault(), "%d mnt", totalMenit)
            view.findViewById<TextView>(R.id.tvHariAktifCount).text = countHari.toString()

            // 3. Ambil Risiko Terbaru
            val latestSkrining = db.hasilSkriningDao().ambilSemuaSkrining(userId).firstOrNull()
            view.findViewById<TextView>(R.id.tvRiskLevelJurnal).text = latestSkrining?.levelRisikoSistem?.name ?: "Rendah"

            // 4. Update Bars Hari Aktif
            updateHariAktifBars(view, userId, since)
        }
    }

    private fun updateHariAktifBars(view: View, userId: String, since: Long) {
        val container = view.findViewById<LinearLayout>(R.id.hariAktifBars)
        container.removeAllViews()

        lifecycleScope.launch {
            val db = MamaFitDatabase.getDatabase(requireContext())
            val allSesi = db.sesiLatihanDao().ambilRiwayatSesi(userId)
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            
            for (i in 0 until 7) {
                val isAktif = allSesi.any { 
                    val c = Calendar.getInstance()
                    c.time = it.waktuSelesai
                    c.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                }

                val bar = View(requireContext())
                val params = LinearLayout.LayoutParams(0, dpToPx(6)).apply {
                    weight = 1f
                    marginEnd = if (i != 6) dpToPx(4) else 0
                }
                bar.layoutParams = params
                bar.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isAktif) R.color.pink_primary else R.color.progress_track
                    )
                )
                container.addView(bar)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }
}
