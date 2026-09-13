package com.mamafit.app

object SkriningData {

    val questions = listOf(
        // A. Data Dasar
        SkriningQuestion(
            id = "q1",
            section = SkriningSection.DATA_DASAR,
            title = "Di trimester berapakah kehamilan Anda saat ini?",
            description = "Pilih periode kehamilan Bunda.",
            options = listOf(
                SkriningOption("t1", R.drawable.ic_trimester1, "Trimester 1", "Minggu 1 - 12"),
                SkriningOption("t2", R.drawable.ic_trimester2, "Trimester 2", "Minggu 13 - 26"),
                SkriningOption("t3", R.drawable.ic_trimester3, "Trimester 3", "Minggu 27 - Kelahiran")
            )
        ),
        SkriningQuestion(
            id = "q2",
            section = SkriningSection.DATA_DASAR,
            title = "Apakah kehamilan ini tunggal atau kembar?",
            description = "Informasi ini membantu menyesuaikan kebutuhan energi Bunda.",
            options = listOf(
                SkriningOption("tunggal", R.drawable.ic_check_circle, "Tunggal"),
                SkriningOption("kembar", R.drawable.ic_warning, "Kembar")
            )
        ),
        SkriningQuestion(
            id = "q3",
            section = SkriningSection.DATA_DASAR,
            title = "Bagaimana riwayat persalinan Bunda sebelumnya?",
            description = "Jika ini kehamilan pertama, pilih 'Belum Pernah'.",
            options = listOf(
                SkriningOption("normal", R.drawable.ic_check_circle, "Normal"),
                SkriningOption("caesar", R.drawable.ic_warning, "Sesar (SC)"),
                SkriningOption("belum_pernah", R.drawable.ic_smile, "Belum Pernah Melahirkan")
            )
        ),

        // B. Skrining Kontraindikasi Absolut
        SkriningQuestion(
            id = "q4",
            section = SkriningSection.KONTRAINDIKASI_ABSOLUT,
            title = "Apakah Bunda memiliki riwayat penyakit jantung yang memengaruhi aliran darah?",
            description = "Penyakit jantung tertentu memerlukan pengawasan ketat saat berolahraga.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_warning, "Ya", isWarning = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak")
            )
        ),
        SkriningQuestion(
            id = "q5",
            section = SkriningSection.KONTRAINDIKASI_ABSOLUT,
            title = "Apakah Bunda memiliki penyakit paru-paru yang membatasi pernapasan?",
            description = "Contoh: sesak napas berat yang sudah terdiagnosis, bukan sekadar sesak ringan biasa saat hamil.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_warning, "Ya", isWarning = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak")
            )
        ),
        SkriningQuestion(
            id = "q6",
            section = SkriningSection.KONTRAINDIKASI_ABSOLUT,
            title = "Apakah Bunda didiagnosis leher rahim lemah (inkompetensi serviks)?",
            description = "Kondisi ini memerlukan pembatasan aktivitas fisik tertentu.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_warning, "Ya", isWarning = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak")
            )
        ),
        SkriningQuestion(
            id = "q7",
            section = SkriningSection.KONTRAINDIKASI_ABSOLUT,
            title = "Bagaimana kondisi tekanan darah Bunda pada pemeriksaan terakhir?",
            description = "Apakah tekanan darah tinggi ini baru muncul selama kehamilan (bukan sebelum hamil)?",
            options = listOf(
                SkriningOption("normal", R.drawable.ic_check_circle, "Normal"),
                SkriningOption("rendah", R.drawable.ic_trend_down, "Rendah"),
                SkriningOption("tinggi_baru", R.drawable.ic_warning, "Tinggi (Baru muncul saat hamil)", isWarning = true),
                SkriningOption("tinggi_lama", R.drawable.ic_warning, "Tinggi (Sudah ada sebelum hamil)")
            )
        ),
        SkriningQuestion(
            id = "q8",
            section = SkriningSection.KONTRAINDIKASI_ABSOLUT,
            title = "Berdasarkan USG terakhir, apakah letak plasenta Bunda normal?",
            description = "Plasenta normal tidak menutupi jalan lahir.",
            options = listOf(
                SkriningOption("normal", R.drawable.ic_check_circle, "Normal"),
                SkriningOption("menutupi", R.drawable.ic_warning, "Menutupi Jalan Lahir", isWarning = true)
            )
        ),
        SkriningQuestion(
            id = "q9",
            section = SkriningSection.KONTRAINDIKASI_ABSOLUT,
            title = "Apakah Bunda mengalami salah satu dari tanda berikut dalam beberapa hari terakhir?",
            description = "Pilih semua yang Bunda rasakan (Checklist).",
            isMultiSelect = true,
            options = listOf(
                SkriningOption("kontraksi", R.drawable.ic_warning, "Kontraksi yang teratur/sering"),
                SkriningOption("nyeri_punggung", R.drawable.ic_warning, "Nyeri punggung bawah yang konstan"),
                SkriningOption("tekanan_panggul", R.drawable.ic_warning, "Tekanan panggul/perut bagian bawah"),
                SkriningOption("kram", R.drawable.ic_warning, "Kram perut"),
                SkriningOption("flek", R.drawable.ic_warning, "Flek atau perdarahan"),
                SkriningOption("ketuban_pecah", R.drawable.ic_warning, "Ketuban pecah (semburan/tetesan)"),
                SkriningOption("keputihan_encer", R.drawable.ic_warning, "Perubahan keputihan (encer/berdarah)"),
                SkriningOption("tidak_ada", R.drawable.ic_check_circle, "Tidak ada gejala di atas")
            )
        ),

        // C. Skrining Kontraindikasi Relatif
        SkriningQuestion(
            id = "q10",
            section = SkriningSection.KONTRAINDIKASI_RELATIF,
            title = "Apakah Bunda memiliki salah satu kondisi berikut?",
            description = "Pilih semua yang sesuai (Checklist).",
            isMultiSelect = true,
            options = listOf(
                SkriningOption("diabetes", R.drawable.ic_medkit, "Diabetes (Tipe 1/2/Gestasional)"),
                SkriningOption("anemia", R.drawable.ic_medkit, "Anemia Berat"),
                SkriningOption("jantung_berdebar", R.drawable.ic_medkit, "Jantung berdebar tidak normal"),
                SkriningOption("tiroid", R.drawable.ic_medkit, "Gangguan Tiroid"),
                SkriningOption("tidak_ada", R.drawable.ic_check_circle, "Tidak ada kondisi di atas")
            )
        ),
        SkriningQuestion(
            id = "q11",
            section = SkriningSection.KONTRAINDIKASI_RELATIF,
            title = "Kontrol Kondisi Kesehatan",
            description = "Untuk kondisi yang dipilih di atas, apakah saat ini terkontrol dengan pengobatan?",
            options = listOf(
                SkriningOption("terkontrol", R.drawable.ic_check_circle, "Ya, Terkontrol"),
                SkriningOption("tidak_terkontrol", R.drawable.ic_warning, "Tidak Terkontrol", isWarning = true),
                SkriningOption("tidak_ada", R.drawable.ic_smile, "Tidak Memiliki Kondisi")
            )
        ),

        // D. Faktor Penyesuaian Intensitas
        SkriningQuestion(
            id = "q12",
            section = SkriningSection.FAKTOR_PENYESUAIAN,
            title = "Sebelum hamil, bagaimana kondisi berat badan Bunda?",
            description = "Gunakan estimasi kategori berat badan Bunda.",
            options = listOf(
                SkriningOption("kurus", R.drawable.ic_favorite, "Sangat Kurus"),
                SkriningOption("normal", R.drawable.ic_favorite, "Normal"),
                SkriningOption("gemuk", R.drawable.ic_warning, "Gemuk - Obesitas")
            )
        ),
        SkriningQuestion(
            id = "q13",
            section = SkriningSection.FAKTOR_PENYESUAIAN,
            title = "Sebelum hamil, seberapa sering Bunda berolahraga?",
            description = "Informasi ini membantu menentukan intensitas awal.",
            options = listOf(
                SkriningOption("tidak_pernah", R.drawable.ic_hourglass, "Tidak Pernah"),
                SkriningOption("jarang", R.drawable.ic_smile, "Jarang"),
                SkriningOption("rutin", R.drawable.ic_bolt, "Rutin")
            )
        ),

        // E. Kondisi Hari Ini
        SkriningQuestion(
            id = "q14",
            section = SkriningSection.KONDISI_HARI_INI,
            title = "Apakah gerakan janin terasa aktif dan normal hari ini?",
            description = "Pantau gerakan si kecil setiap hari.",
            options = listOf(
                SkriningOption("aktif", R.drawable.ic_bolt, "Ya, Aktif"),
                SkriningOption("kurang_aktif", R.drawable.ic_warning, "Kurang Aktif", isWarning = true),
                SkriningOption("belum_terasa", R.drawable.ic_hourglass, "Belum Terasa (UK Kecil)")
            )
        ),
        SkriningQuestion(
            id = "q15",
            section = SkriningSection.KONDISI_HARI_INI,
            title = "Apakah Bunda merasa pusing, pandangan kabur, atau sakit kepala hebat hari ini?",
            description = "Gejala ini penting untuk dipantau demi keamanan Bunda.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_warning, "Ya", isWarning = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak")
            )
        ),
        SkriningQuestion(
            id = "q16",
            section = SkriningSection.KONDISI_HARI_INI,
            title = "Apakah ada nyeri tulang kemaluan atau punggung yang sangat mengganggu hari ini?",
            description = "Nyeri hebat perlu perhatian khusus saat bergerak.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_warning, "Ya", isWarning = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak")
            )
        ),
        SkriningQuestion(
            id = "q17",
            section = SkriningSection.KONDISI_HARI_INI,
            title = "Secara keseluruhan, apakah Bunda merasa cukup bertenaga untuk berolahraga hari ini?",
            description = "Dengarkan kondisi tubuh Bunda hari ini.",
            options = listOf(
                SkriningOption("sangat_siap", R.drawable.ic_bolt, "Sangat Siap"),
                SkriningOption("cukup", R.drawable.ic_smile, "Cukup"),
                SkriningOption("lelah", R.drawable.ic_bed, "Lelah / Lemas")
            ),
            nextButtonLabel = "Lihat Hasil Skrining"
        )
    )
}
