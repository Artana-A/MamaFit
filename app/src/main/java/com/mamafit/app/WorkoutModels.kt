package com.mamafit.app

import java.util.Date

sealed class KatalogGerakan {
    abstract val idGerakan: String
    abstract val namaGerakan: String
    abstract val levelRisikoMinimal: LevelRisiko

    data class GerakanAktif(
        override val idGerakan: String,
        override val namaGerakan: String,
        override val levelRisikoMinimal: LevelRisiko,
        val daftarTrimesterCocok: List<Int>,
        val durasiMenit: Int,
        val tautanVideoPanduan: String
    ) : KatalogGerakan()

    data class GerakanRingan(
        override val idGerakan: String,
        override val namaGerakan: String,
        override val levelRisikoMinimal: LevelRisiko,
        val aktivitasRumahDasar: String,
        val targetDurasiMenit: Int
    ) : KatalogGerakan()
}

enum class ModeLatihan {
    GERAKAN_AKTIF,
    GERAKAN_RINGAN
}

enum class StatusLatihan {
    SELESAI,
    DIHENTIKAN_KARENA_BAHAYA,
    DIHENTIKAN_BUNAS_LELAH
}
