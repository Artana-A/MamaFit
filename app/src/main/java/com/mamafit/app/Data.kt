package com.mamafit.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

// Enum
@Serializable
enum class Trimester {
    SATU,
    DUA,
    TIGA
}

@Serializable
enum class TekananDarah {
    NORMAL,
    RENDAH,
    TINGGI
}

@Serializable
enum class LetakPlasenta {
    NORMAL,
    PREVIA,
    TIDAK_TAHU
}

@Serializable
enum class GerakanJanin {
    AKTIF,
    KURANG_AKTIF,
    BELUM_TERASA
}

@Serializable
enum class LevelRisiko {
    RENDAH,
    SEDANG,
    TINGGI
}

@Serializable
enum class StatusValidasi {
    MENUNGGU_VALIDASI,
    DISETUJUI,
    PERLU_PENYESUAIAN
}

@Serializable
enum class StatusLatihan {
    BERLANGSUNG,
    SELESAI,
    DIHENTIKAN_KARENA_BAHAYA
}

@Serializable
enum class ModeLatihan {
    GERAKAN_AKTIF,
    GERAKAN_RINGAN
}

// Akun Pengguna [1]
@Serializable
@Entity(tableName = "pengguna")
data class Pengguna(
    @PrimaryKey val namaPengguna: String,
    val idPengguna: String,
    val namaLengkap: String,
    val nomorTelepon: String,
    val email: String,
    val kataSandi: String,
    @Serializable(with = DateSerializer::class)
    val tanggalDaftar: Date
)

// Helper for Skrining
@Serializable
data class RiwayatKesehatan(
    val penyakitJantung: Boolean = false,
    val letakPlasenta: LetakPlasenta = LetakPlasenta.NORMAL
)

@Serializable
data class GejalaSaatIni(
    val tekananDarah: TekananDarah = TekananDarah.NORMAL,
    val pendarahan: Boolean = false,
    val nyeriPerutHebat: Boolean = false,
    val pusingBerat: Boolean = false,
    val gerakanJanin: GerakanJanin = GerakanJanin.AKTIF,
    val nyeriTulangKemaluan: Boolean = false,
    val bertenaga: Boolean = true
)

// Skrining Awal [2] Re-check bulanan [6-7]
@Serializable
data class HasilSkrining(
    val idSkrining: String,
    val idPengguna: String,
    val periodeBulan: String,
    val trimester: Trimester,
    val beratBadanKg: Float,
    val riwayatKesehatan: RiwayatKesehatan,
    val gejalaSaatIni: GejalaSaatIni,
    val levelRisikoSistem: LevelRisiko,
    val apakahPemeriksaanUlangBulanan: Boolean,
    @Serializable(with = DateSerializer::class)
    val tanggalPengisian: Date
)

// Validasi Bidan [8-9]
@Serializable
data class ValidasiBidan(
    val idValidasi: String,
    val idSkrining: String,
    val idBidan: String,
    val statusValidasi: StatusValidasi,
    val levelRisikoFinal: LevelRisiko,
    val catatanPenyesuaian: String?,
    @Serializable(with = DateSerializer::class)
    val tanggalValidasi: Date?
)

// Gerakan Senam
@Serializable
sealed class KatalogGerakan {
    abstract val idGerakan: String
    abstract val namaGerakan: String
    abstract val levelRisikoMinimal: LevelRisiko

    @Serializable
    data class GerakanAktif(
        override val idGerakan: String,
        override val namaGerakan: String,
        override val levelRisikoMinimal: LevelRisiko,
        val daftarTrimesterCocok: List<Int>,
        val durasiMenit: Int,
        val tautanVideoPanduan: String
    ) : KatalogGerakan()

    @Serializable
    data class GerakanRingan(
        override val idGerakan: String,
        override val namaGerakan: String,
        override val levelRisikoMinimal: LevelRisiko,
        val aktivitasRumahDasar: String,
        val targetDurasiMenit: Int
    ) : KatalogGerakan()
}

// Sesi Latihan [10-13]
@Serializable
data class TandaBahaya(
    val idTandaBahaya: String,
    val idSesi: String,
    val jenisTandaBahaya: String,
    @Serializable(with = DateSerializer::class)
    val waktuTerdeteksi: Date
)

@Serializable
data class SesiLatihan(
    val idSesi: String,
    val idPengguna: String,
    val idGerakan: String,
    val modeLatihan: ModeLatihan,
    val statusLatihan: StatusLatihan,
    @Serializable(with = DateSerializer::class)
    val waktuMulai: Date,
    @Serializable(with = DateSerializer::class)
    val waktuSelesai: Date?,
    val kaloriTerbakar: Float = 0f,
    val durasiMenit: Int = 0,
    val skorPostur: Int = 0,
    val masukanKecerdasanBuatan: String? = null
)

// Lapran Mingguan [16]
@Serializable
data class LaporanMingguan(
    val idLaporan: String,
    val idPengguna: String,
    val mingguKe: String,
    val totalSesi: Int,
    val totalDurasiMenit: Int,
    val rataRataSkorPostur: Float
)

// Fitur Tambahan
@Serializable
data class TipsKesehatan(
    val idTips: String,
    val judulTips: String,
    val isiTips: String,
    val relevanUntukTrimester: Int?,
    val relevanUntukLevelRisiko: LevelRisiko?
)

@Serializable
data class PengingatLatihan(
    val idPengingat: String,
    val idPengguna: String,
    val daftarJadwalHari: List<String>,
    val jamPengingat: String,
    val apakahAktif: Boolean
)

// Serializer for Date
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)
    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}
