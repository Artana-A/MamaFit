package com.mamafit.app

object SkriningData {

    val questions = listOf(
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Di trimester berapakah kehamilan Anda saat ini?",
            description = "Pilih periode kehamilan Anda untuk mendapatkan program latihan dan nutrisi yang dipersonalisasi sesuai kebutuhan Bunda.",
            options = listOf(
                SkriningOption("t1", R.drawable.ic_trimester1, "Trimester 1", "Minggu 1 - 12"),
                SkriningOption("t2", R.drawable.ic_trimester2, "Trimester 2", "Minggu 13 - 26"),
                SkriningOption("t3", R.drawable.ic_trimester3, "Trimester 3", "Minggu 27 - Kelahiran")
            )
        ),
        SkriningQuestion(
            section = SkriningSection.RIWAYAT_KESEHATAN,
            title = "Apakah Bunda memiliki riwayat penyakit jantung atau sesak napas?",
            description = "Informasi ini membantu kami menyesuaikan intensitas latihan agar tetap aman untuk Bunda.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_medkit, "Ya", squareIcon = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak", squareIcon = true)
            )
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Bagaimana kondisi tekanan darah Bunda pada pemeriksaan terakhir?",
            description = "Informasi ini membantu kami menyesuaikan intensitas latihan yang aman untuk kondisi Bunda saat ini.",
            options = listOf(
                SkriningOption("normal", R.drawable.ic_favorite, "Normal", "90/60 - 120/80 mmHg", badge = "Paling Aman"),
                SkriningOption("rendah", R.drawable.ic_trend_down, "Rendah", "Di bawah 90/60 mmHg"),
                SkriningOption("tinggi", R.drawable.ic_trend_up, "Tinggi", "Di atas 120/80 mmHg")
            ),
            noteTitle = "Catatan Medis",
            note = "Jika Bunda tidak yakin, kami sarankan untuk melakukan pengecekan di fasilitas kesehatan terdekat sebelum melanjutkan program latihan berat."
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Apakah Bunda mengalami flek atau pendarahan dalam 48 jam terakhir?",
            description = "Kami perlu memastikan keamanan Bunda dan janin sebelum menyarankan program olahraga yang tepat.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_warning, "Ya, saya mengalaminya", squareIcon = true),
                SkriningOption("tidak", R.drawable.ic_check_circle, "Tidak ada pendarahan", squareIcon = true)
            ),
            noteTitle = "Catatan Penting",
            note = "Kejujuran Bunda sangat penting demi kesehatan kehamilan Bunda. Jika terjadi pendarahan, segera hubungi tenaga medis."
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Apakah Bunda merasakan kontraksi yang sering atau nyeri perut hebat?",
            description = "Kontraksi atau nyeri yang tidak biasa perlu kami pantau demi keamanan latihan Bunda.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_check_circle, "Ya", "Saya merasa nyeri atau kontraksi yang teratur."),
                SkriningOption("tidak", R.drawable.ic_smile, "Tidak", "Perut saya terasa normal dan tidak ada nyeri hebat.")
            ),
            noteTitle = "Tips Kehamilan",
            note = "Kontraksi palsu (Braxton Hicks) biasanya tidak teratur dan hilang saat beristirahat. Namun, jika nyeri hebat dan teratur, segera hubungi dokter."
        ),
        SkriningQuestion(
            section = SkriningSection.RIWAYAT_KESEHATAN,
            title = "Letak Plasenta Bunda",
            description = "Berdasarkan USG terakhir, apakah letak plasenta Bunda normal (tidak menutupi jalan lahir)?",
            options = listOf(
                SkriningOption("normal", R.drawable.ic_check_circle, "Normal", "Plasenta berada di posisi yang tepat"),
                SkriningOption("previa", R.drawable.ic_warning, "Plasenta Previa", "Menutupi sebagian/seluruh jalan lahir"),
                SkriningOption("tidak_tahu", R.drawable.ic_help, "Tidak Tahu", "Belum melakukan USG atau tidak yakin")
            )
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Apakah gerakan janin terasa aktif dan normal hari ini?",
            description = "Informasi ini membantu kami memantau kesehatan si kecil setiap hari.",
            options = listOf(
                SkriningOption("aktif", R.drawable.ic_bolt, "Ya, Aktif", "Gerakan terasa kuat dan teratur"),
                SkriningOption("kurang_aktif", R.drawable.ic_qr, "Kurang Aktif", "Terasa namun lebih lemah dari biasanya"),
                SkriningOption("belum_terasa", R.drawable.ic_hourglass, "Belum Terasa (UK Kecil)", "Hanya getaran halus atau belum terasa")
            ),
            noteTitle = "Info",
            note = "Konsultasikan dengan dokter jika Anda merasa khawatir tentang intensitas gerakan bayi Anda."
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Apakah Bunda merasa pusing, pandangan kabur, atau sakit kepala hebat?",
            description = "Gejala ini penting untuk dipantau demi kesehatan Bunda dan buah hati.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_medkit, "Ya", "Saya merasakan salah satu gejala tersebut"),
                SkriningOption("tidak", R.drawable.ic_close, "Tidak", "Kondisi saya saat ini baik-baik saja")
            ),
            noteTitle = "Info",
            note = "Informasi ini akan membantu tim medis kami memberikan rekomendasi yang paling tepat untuk menjaga tekanan darah dan kesehatan Bunda."
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Apakah ada nyeri pada tulang kemaluan atau punggung yang sangat mengganggu?",
            description = "Nyeri sendi dan tulang umum terjadi selama kehamilan, namun kami ingin memastikan kenyamanan Anda selama berolahraga.",
            options = listOf(
                SkriningOption("ya", R.drawable.ic_check_circle, "Ya", "Saya merasakan nyeri yang cukup signifikan"),
                SkriningOption("tidak", R.drawable.ic_close, "Tidak", "Kondisi saya baik-baik saja saat ini")
            )
        ),
        SkriningQuestion(
            section = SkriningSection.KEADAAN_SAAT_INI,
            title = "Secara keseluruhan, apakah Bunda merasa cukup bertenaga untuk berolahraga hari ini?",
            description = "Kejujuran Bunda membantu kami menyesuaikan intensitas latihan agar tetap aman dan nyaman.",
            options = listOf(
                SkriningOption("sangat_siap", R.drawable.ic_bolt, "Sangat Siap", "Penuh energi dan siap berkeringat!"),
                SkriningOption("cukup", R.drawable.ic_smile, "Cukup", "Biasa saja, tapi sanggup bergerak sedikit."),
                SkriningOption("lelah", R.drawable.ic_bed, "Lelah/Lemas", "Butuh istirahat atau latihan yang sangat ringan.")
            ),
            noteTitle = "Info",
            note = "Menyelesaikan kuesioner ini membantu algoritma kami menentukan level sirkuit latihan yang paling aman untuk kondisi kehamilan Bunda hari ini.",
            nextButtonLabel = "Lihat Hasil Personalisasi"
        )
    )
}
