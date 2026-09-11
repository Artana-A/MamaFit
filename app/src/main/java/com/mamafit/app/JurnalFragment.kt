package com.mamafit.app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class JurnalFragment : Fragment(R.layout.fragment_jurnal) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sembunyikan wordmark "MamaFit" di top bar (desain Jurnal cuma avatar + bell)
        view.findViewById<View>(R.id.tvTopBarLogo).visibility = View.GONE

        setupHariAktif(view)
        setupMood(view)
        setupKalender(view)

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
        var selectedIndex = 3 // sesuai desain, mood ke-4 terpilih

        fun render() {
            container.removeAllViews()
            moods.forEachIndexed { i, emoji ->
                val itemView = layoutInflater.inflate(R.layout.item_mood, container, false)
                val tv = itemView.findViewById<TextView>(R.id.tvMoodEmoji)
                tv.text = emoji
                tv.setBackgroundResource(
                    if (i == selectedIndex) R.drawable.bg_avatar_circle else R.drawable.bg_icon_circle_light
                )
                itemView.setOnClickListener {
                    selectedIndex = i
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
        val tanggal = listOf(12, 13, 14, 15, 16, 17, 18)
        val selectedIndex = 3 // tanggal 15

        tanggal.forEachIndexed { i, tgl ->
            val itemView = layoutInflater.inflate(R.layout.item_calendar_day, container, false)
            itemView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
            itemView.findViewById<TextView>(R.id.tvDayLabel).text = hariLabel[i]
            val tvDate = itemView.findViewById<TextView>(R.id.tvDayDate)
            tvDate.text = tgl.toString()
            if (i == selectedIndex) {
                tvDate.setBackgroundResource(R.drawable.bg_avatar_circle)
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            container.addView(itemView)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}