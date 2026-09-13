package com.mamafit.app

enum class SkriningType {
    AWAL,
    BULANAN
}

enum class SkriningSection(val displayName: String) {
    DATA_DASAR("A. Data Dasar"),
    KONTRAINDIKASI_ABSOLUT("B. Skrining Kontraindikasi Absolut"),
    KONTRAINDIKASI_RELATIF("C. Skrining Kontraindikasi Relatif"),
    FAKTOR_PENYESUAIAN("D. Faktor Penyesuaian Intensitas"),
    KONDISI_HARI_INI("E. Kondisi Hari Ini")
}

data class SkriningOption(
    val id: String,
    val iconRes: Int,
    val title: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val squareIcon: Boolean = false,
    val isWarning: Boolean = false
)

data class SkriningQuestion(
    val id: String,
    val section: SkriningSection,
    val title: String,
    val description: String,
    val options: List<SkriningOption>,
    val isMultiSelect: Boolean = false,
    val noteTitle: String? = null,
    val note: String? = null,
    val nextButtonLabel: String = "Lanjut",
    val followUpQuestion: String? = null
)
