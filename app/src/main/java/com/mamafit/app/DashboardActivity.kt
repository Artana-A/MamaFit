package com.mamafit.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Menggunakan findViewById agar tidak error 'Unresolved reference'
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val startTab = intent.getStringExtra(EXTRA_START_TAB) ?: TAB_BERANDA

        openFragment(fragmentForTab(startTab))
        bottomNav.selectedItemId = menuIdForTab(startTab)

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_beranda -> BerandaFragment()
                R.id.nav_olahraga -> OlahragaFragment()
                R.id.nav_jurnal -> JurnalFragment()
                R.id.nav_profil -> ProfilFragment()
                else -> BerandaFragment()
            }
            openFragment(fragment)
            true
        }
    }

    private fun openFragment(fragment: Fragment) {
        // Menggunakan R.id.fragmentContainer untuk transaksi fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun fragmentForTab(tab: String): Fragment = when (tab) {
        TAB_OLAHRAGA -> OlahragaFragment()
        TAB_JURNAL -> JurnalFragment()
        TAB_PROFIL -> ProfilFragment()
        else -> BerandaFragment()
    }

    private fun menuIdForTab(tab: String): Int = when (tab) {
        TAB_OLAHRAGA -> R.id.nav_olahraga
        TAB_JURNAL -> R.id.nav_jurnal
        TAB_PROFIL -> R.id.nav_profil
        else -> R.id.nav_beranda
    }

    companion object {
        const val EXTRA_START_TAB = "extra_start_tab"
        const val TAB_BERANDA = "beranda"
        const val TAB_OLAHRAGA = "olahraga"
        const val TAB_JURNAL = "jurnal"
        const val TAB_PROFIL = "profil"
    }
}