package com.mamafit.app

object WorkoutData {
    
    val listGerakan = listOf(
        // GERAKAN AKTIF (Trimester 2 - Resiko Rendah)
        Gerakan.GerakanAktif(
            idGerakan = "GA01",
            nama = "Panggul Miring (Pelvic Tilt)",
            levelRisikoMinimal = LevelRisiko.RENDAH,
            trimesterCocok = listOf(1, 2, 3),
            durasiMenit = 5,
            videoPanduanUrl = "dummy_url_1"
        ),
        Gerakan.GerakanAktif(
            idGerakan = "GA02",
            nama = "Squat Hamil Ringan",
            levelRisikoMinimal = LevelRisiko.RENDAH,
            trimesterCocok = listOf(2),
            durasiMenit = 7,
            videoPanduanUrl = "dummy_url_2"
        ),
        
        // GERAKAN RINGAN / REFRAMING (Untuk Resiko Sedang / Aktivitas Rumah)
        Gerakan.GerakanRingan(
            idGerakan = "GR01",
            nama = "Posisi Duduk Ergonomis",
            levelRisikoMinimal = LevelRisiko.SEDANG,
            aktivitasRumahDasar = "Duduk saat bekerja atau bersantai",
            targetDurasiMenit = 10
        ),
        Gerakan.GerakanRingan(
            idGerakan = "GR02",
            nama = "Melipat Pakaian (Postur Tegak)",
            levelRisikoMinimal = LevelRisiko.SEDANG,
            aktivitasRumahDasar = "Aktivitas rumah tangga ringan",
            targetDurasiMenit = 15
        )
    )

    fun getGerakanByRisiko(level: LevelRisiko): List<Gerakan> {
        return listGerakan.filter { it.levelRisikoMinimal <= level }
    }
}
