package com.mamafit.app

object WorkoutData {
    
    val listGerakan = listOf(
        // GERAKAN AKTIF (Trimester 2 - Resiko Rendah)
        KatalogGerakan.GerakanAktif(
            idGerakan = "GA01",
            namaGerakan = "Panggul Miring (Pelvic Tilt)",
            levelRisikoMinimal = LevelRisiko.RENDAH,
            daftarTrimesterCocok = listOf(1, 2, 3),
            durasiMenit = 5,
            tautanVideoPanduan = "dummy_url_1"
        ),
        KatalogGerakan.GerakanAktif(
            idGerakan = "GA02",
            namaGerakan = "Squat Hamil Ringan",
            levelRisikoMinimal = LevelRisiko.RENDAH,
            daftarTrimesterCocok = listOf(2),
            durasiMenit = 7,
            tautanVideoPanduan = "dummy_url_2"
        ),
        
        // GERAKAN RINGAN / REFRAMING (Untuk Resiko Sedang / Aktivitas Rumah)
        KatalogGerakan.GerakanRingan(
            idGerakan = "GR01",
            namaGerakan = "Posisi Duduk Ergonomis",
            levelRisikoMinimal = LevelRisiko.SEDANG,
            aktivitasRumahDasar = "Duduk saat bekerja atau bersantai",
            targetDurasiMenit = 10
        ),
        KatalogGerakan.GerakanRingan(
            idGerakan = "GR02",
            namaGerakan = "Melipat Pakaian (Postur Tegak)",
            levelRisikoMinimal = LevelRisiko.SEDANG,
            aktivitasRumahDasar = "Aktivitas rumah tangga ringan",
            targetDurasiMenit = 15
        )
    )

    fun getGerakanByRisiko(level: LevelRisiko): List<KatalogGerakan> {
        return listGerakan.filter { it.levelRisikoMinimal <= level }
    }
}
