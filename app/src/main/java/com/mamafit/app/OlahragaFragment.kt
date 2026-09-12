package com.mamafit.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

data class ExerciseItem(
    val imageRes: Int,
    val title: String,
    val duration: String,
    val intensity: String
)

class OlahragaFragment : Fragment(R.layout.fragment_olahraga) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderExerciseList(view, ModeLatihan.GERAKAN_AKTIF)

        setupToggle(view)
    }

    private fun renderExerciseList(view: View, mode: ModeLatihan) {
        val exercises = WorkoutData.listGerakan.filter { 
            if (mode == ModeLatihan.GERAKAN_AKTIF) it is KatalogGerakan.GerakanAktif 
            else it is KatalogGerakan.GerakanRingan 
        }

        val containers = listOf(
            view.findViewById<View>(R.id.exercise1),
            view.findViewById<View>(R.id.exercise2),
            view.findViewById<View>(R.id.exercise3)
        )

        // Reset visibility
        containers.forEach { it.visibility = View.GONE }

        exercises.forEachIndexed { i, item ->
            if (i >= containers.size) return@forEachIndexed
            val card = containers[i]
            card.visibility = View.VISIBLE
            
            card.findViewById<TextView>(R.id.tvExerciseTitle).text = item.namaGerakan
            
            when (item) {
                is KatalogGerakan.GerakanAktif -> {
                    card.findViewById<ImageView>(R.id.ivExerciseImage).setImageResource(R.drawable.img_exercise_1)
                    card.findViewById<TextView>(R.id.tvExerciseDuration).text = "${item.durasiMenit} Menit"
                    card.findViewById<TextView>(R.id.tvExerciseIntensity).text = item.levelRisikoMinimal.name
                }
                is KatalogGerakan.GerakanRingan -> {
                    card.findViewById<ImageView>(R.id.ivExerciseImage).setImageResource(R.drawable.ic_home)
                    card.findViewById<TextView>(R.id.tvExerciseDuration).text = "${item.targetDurasiMenit} Menit"
                    card.findViewById<TextView>(R.id.tvExerciseIntensity).text = "Reframing"
                }
            }

            card.setOnClickListener {
                val intent = Intent(requireContext(), SesiOlahragaActivity::class.java)
                intent.putExtra("EXTRA_EXERCISE_ID", item.idGerakan)
                intent.putExtra("EXTRA_EXERCISE_NAME", item.namaGerakan)
                startActivity(intent)
            }
        }
    }

    private fun setupToggle(view: View) {
        val toggleAktif = view.findViewById<View>(R.id.toggleAktif)
        val toggleRingan = view.findViewById<View>(R.id.toggleRingan)
        val tvAktif = view.findViewById<TextView>(R.id.tvToggleAktif)
        val tvRingan = view.findViewById<TextView>(R.id.tvToggleRingan)

        toggleAktif.setOnClickListener {
            toggleAktif.setBackgroundResource(R.drawable.bg_button_primary)
            toggleRingan.background = null
            tvAktif.setTextColor(resources.getColor(R.color.white, null))
            tvRingan.setTextColor(resources.getColor(R.color.text_gray, null))
            renderExerciseList(view, ModeLatihan.GERAKAN_AKTIF)
        }

        toggleRingan.setOnClickListener {
            toggleRingan.setBackgroundResource(R.drawable.bg_button_primary)
            toggleAktif.background = null
            tvRingan.setTextColor(resources.getColor(R.color.white, null))
            tvAktif.setTextColor(resources.getColor(R.color.text_gray, null))
            renderExerciseList(view, ModeLatihan.GERAKAN_RINGAN)
        }
    }
}