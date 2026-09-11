package com.mamafit.app
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Enum
/*
enum class TipeAkun {
    IBU_HAMIL,
    BIDAN
}
*/
enum class Trimester {
    SATU,
    DUA,
    TIGA
}

enum class TekananDarah {
    NORMAL,
    RENDAH,
    TINGGI
}

enum class LetakPlasenta {
    NORMAL,
    PREVIA,
    TIDAK_TAHU
}

enum class GerakanJanin {
    AKTIF,
    KURANG_AKTIF,
    BELUM_TERASA
}

enum class LevelRisiko {
    RENDAH,
    SEDANG,
    TINGGI
}

enum class StatusValidasiBidan {
    MENUNGGU_VALIDASI,
    DISETUJUI,
    PERLU_PENYESUAIAN
}

enum class StatusSesiLatihan {
    BERLANGSUNG,
    SELESAI,
    DIHENTIKAN_KARENA_BAHAYA
}

enum class ModeLatihan {
    GERAKAN_AKTIF,
    GERAKAN_RINGAN // reframing aktivitas rumah / posisi duduk
}

// Akun Pengguna [1]
@Entity(tableName = "pengguna")
data class Pengguna(
    @PrimaryKey val username: String,
    val idPengguna: String,
    val nama: String,
    val noHp: String,
    val email: String,
    val password: String,
    val tanggalDaftar: Date
)

// Skrining Awal [2] Re-check bulanan [6-7]
data class RiwayatKesehatan(
    val penyakitJantung: Boolean = false,
    val letakPlasenta: LetakPlasenta,
    // val hipertensi: Boolean = false,
    // val diabetesGestasional: Boolean = false,
    // val riwayatKeguguran: Boolean = false,
    // val gangguanTiroid: Boolean = false,
    // val riwayatOperasiKandungan: Boolean = false,
    // val lainnya: String? = null
)

data class GejalaSaatIni(
    // val sesakNapas: Boolean = false,
    val tekananDarah: TekananDarah,
    val pendarahan: Boolean = false,
    val nyeriPerutHebat: Boolean = false,
    val pusingBerat: Boolean = false,
    // val kontraksiDini: Boolean = false,
    val gerakanJanin: GerakanJanin,
    val nyeriTulangKemaluan: Boolean,
    val bertenaga: Boolean,
    // val lainnya: String? = null
)

data class JawabanSkrining(
    val trimester: Trimester,
    val beratBadanKg: Float,
    val riwayatKesehatan: RiwayatKesehatan,
    val gejalaSaatIni: GejalaSaatIni
)

data class HasilSkrining(
    val idSkrining: String,
    val idPengguna: String,
    val periodeBulan: String,
    val jawaban: JawabanSkrining,
    val levelRisikoSistem: LevelRisiko,
    val isReCheckBulanan: Boolean,
    val tanggalPengisian: Date
)

// Validasi Bidan [8-9]
data class ValidasiBidan(
    val idValidasi: String,
    val idSkrining: String,
    val idBidan: String,
    val status: StatusValidasiBidan,
    val levelRisikoFinal: LevelRisiko,
    val catatanPenyesuaian: String?,
    val tanggalValidasi: Date?
)

// Gerakan Senam
sealed class Gerakan {
    abstract val idGerakan: String
    abstract val nama: String
    abstract val levelRisikoMinimal: LevelRisiko

    data class GerakanAktif(
        override val idGerakan: String,
        override val nama: String,
        override val levelRisikoMinimal: LevelRisiko,
        val trimesterCocok: List<Int>,
        val durasiMenit: Int,
        val videoPanduanUrl: String
    ) : Gerakan()

    data class GerakanRingan(
        override val idGerakan: String,
        override val nama: String,
        override val levelRisikoMinimal: LevelRisiko,
        val aktivitasRumahDasar: String,
        val targetDurasiMenit: Int
    ) : Gerakan()
}

// Sesi Latihan [10-13]
data class TandaBahaya(
    val jenis: String,
    val waktuTerdeteksi: Date
)

data class SesiLatihan(
    val idSesi: String,
    val idPengguna: String,
    val idGerakan: String,
    val mode: ModeLatihan,
    val status: StatusSesiLatihan,
    val waktuMulai: Date,
    val waktuSelesai: Date?,
    val tandaBahayaTerdeteksi: List<TandaBahaya> = emptyList()
)

// Hasil Sesi Latihan [14] dan Riwayat Aktifitas [15]
data class HasilSesi(
    val idSesi: String,
    val kaloriTerbakar: Float,
    val durasiMenit: Int,
    val skorPostur: Int
)

data class RiwayatAktivitas(
    val idPengguna: String,
    val daftarSesi: List<HasilSesi>
)

// Lapran Mingguan [16]
data class LaporanMingguan(
    val idLaporan: String,
    val idPengguna: String,
    val mingguKe: String,
    val totalSesi: Int,
    val totalDurasiMenit: Int,
    val rataRataSkorPostur: Float,
    val ringkasanSkrining: HasilSkrining?
)

// Fitur Tambahan
data class TipsKesehatan(
    val idTips: String,
    val judul: String,
    val isi: String,
    val relevanUntukTrimester: Int?,
    val relevanUntukLevelRisiko: LevelRisiko?
)

data class PengingatLatihan(
    val idPengingat: String,
    val idPengguna: String,
    val jadwalHari: List<String>,
    val jamPengingat: String,
    val aktif: Boolean
)