package com.mamafit.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class ProfilFragment : Fragment(R.layout.fragment_profil) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProfileData(view)
        setupRow(view, R.id.rowAkun, R.drawable.ic_person, "Akun", danger = false)
        setupRow(view, R.id.rowNotifikasi, R.drawable.ic_bell, "Notifikasi", danger = false)
        setupRow(view, R.id.rowBantuan, R.drawable.ic_help, "Bantuan", danger = false)
        
        // Setup tombol Keluar dengan logika logout
        setupRow(view, R.id.rowKeluar, R.drawable.ic_logout, "Keluar", danger = true)
        view.findViewById<View>(R.id.rowKeluar).setOnClickListener {
            performLogout()
        }

        view.findViewById<View>(R.id.btnUpgrade).setOnClickListener {
            // TODO: buka halaman upgrade Pro
        }
    }

    private fun setupProfileData(view: View) {
        val prefs = requireContext().getSharedPreferences("mamafit_prefs", Context.MODE_PRIVATE)
        val fullName = prefs.getString("user_name", "Mama")
        view.findViewById<TextView>(R.id.tvProfileName).text = fullName

        // Ambil data trimester dari SharedPreferences
        val trimester = prefs.getString("user_trimester", "Satu")
        val tvTrimester = view.findViewById<TextView>(R.id.tvTrimesterBadge)
        tvTrimester.text = "Trimester $trimester"
    }

    private fun performLogout() {
        // Hapus session dari SharedPreferences
        val prefs = requireContext().getSharedPreferences("mamafit_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            // Bisa hapus data lain jika perlu, misal: remove("user_name")
            apply()
        }

        Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show()

        // Kembali ke MainActivity (halaman awal/login)
        val intent = Intent(requireContext(), MainActivity::class.java)
        // Clear task agar user tidak bisa tekan tombol back kembali ke profil
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupRow(root: View, containerId: Int, iconRes: Int, label: String, danger: Boolean) {
        val row = root.findViewById<View>(containerId)
        row.findViewById<ImageView>(R.id.ivRowIcon).setImageResource(iconRes)
        val tvLabel = row.findViewById<TextView>(R.id.tvRowLabel)
        tvLabel.text = label

        val color = if (danger) R.color.pink_primary else R.color.text_dark
        tvLabel.setTextColor(ContextCompat.getColor(requireContext(), color))

        if (danger) {
            row.findViewById<ImageView>(R.id.ivRowIcon)
                .setColorFilter(ContextCompat.getColor(requireContext(), R.color.pink_primary))
        }

        row.setOnClickListener {
            // TODO: navigasi sesuai menu (Akun/Notifikasi/Bantuan/Keluar)
        }
    }
}