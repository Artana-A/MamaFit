package com.mamafit.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class BerandaFragment : Fragment(R.layout.fragment_beranda) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting(view)

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

        // Update trimester di Beranda
        val trimester = prefs.getString("user_trimester", "1")
        view.findViewById<TextView>(R.id.tvGreetingSub).text = "Trimester $trimester • Minggu ke 24"
    }
}