package com.mamafit.app

enum class SkriningSection(val displayName: String) {
    RIWAYAT_KESEHATAN("Riwayat Kesehatan"),
    KEADAAN_SAAT_INI("Keadaan Saat Ini")
}

data class SkriningOption(
    val id: String,
    val iconRes: Int,
    val title: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val squareIcon: Boolean = false
)

data class SkriningQuestion(
    val section: SkriningSection,
    val title: String,
    val description: String,
    val options: List<SkriningOption>,
    val noteTitle: String? = null,
    val note: String? = null,
    val nextButtonLabel: String = "Lanjut"
)
