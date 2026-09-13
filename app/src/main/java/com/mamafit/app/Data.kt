package com.mamafit.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

@Serializable
enum class Trimester {
    @SerialName("1") SATU,
    @SerialName("2") DUA,
    @SerialName("3") TIGA
}

@Serializable
enum class TekananDarah {
    @SerialName("normal") NORMAL,
    @SerialName("rendah") RENDAH,
    @SerialName("tinggi") TINGGI,
    @SerialName("hipertensi_gestasional") HIPERTENSI_GESTASIONAL,
    @SerialName("kronis") KRONIS
}

@Serializable
enum class JenisKehamilan {
    @SerialName("tunggal") TUNGGAL,
    @SerialName("kembar") KEMBAR
}

@Serializable
enum class RiwayatPersalinan {
    @SerialName("normal") NORMAL,
    @SerialName("sesar") SESAR,
    @SerialName("belum_pernah") BELUM_PERNAH
}

@Serializable
enum class BbSebelumHamil {
    @SerialName("sangat_kurus") SANGAT_KURUS,
    @SerialName("normal") NORMAL,
    @SerialName("gemuk_obesitas") GEMUK_OBESITAS
}

@Serializable
enum class FrekuensiOlahraga {
    @SerialName("tidak_pernah") TIDAK_PERNAH,
    @SerialName("jarang") JARANG,
    @SerialName("rutin") RUTIN
}

@Serializable
enum class LevelRisiko {
    @SerialName("rendah") RENDAH,
    @SerialName("sedang") SEDANG,
    @SerialName("tinggi") TINGGI
}

// Akun Pengguna (Mapping ke Tabel SQL)
@Serializable
@Entity(tableName = "pengguna")
data class Pengguna(
    @SerialName("nama_pengguna") @PrimaryKey val namaPengguna: String,
    @SerialName("id_pengguna") val idPengguna: String,
    @SerialName("nama_lengkap") val namaLengkap: String,
    @SerialName("nomor_telepon") val nomorTelepon: String,
    @SerialName("email") val email: String,
    @SerialName("kata_sandi") val kataSandi: String,
    @SerialName("tanggal_daftar") @Serializable(with = DateSerializer::class) val tanggalDaftar: Date
)

// Struktur Data Skrining yang Flat (Sesuai SQL)
@Serializable
data class RiwayatKesehatan(
    @SerialName("jenis_kehamilan") val jenisKehamilan: JenisKehamilan = JenisKehamilan.TUNGGAL,
    @SerialName("riwayat_persalinan") val riwayatPersalinan: RiwayatPersalinan = RiwayatPersalinan.BELUM_PERNAH,
    @SerialName("riwayat_jantung") val riwayatJantung: Boolean = false,
    @SerialName("penyakit_paru_berat") val penyakitParuBerat: Boolean = false,
    @SerialName("inkompetensi_serviks") val inkompetensiServiks: Boolean = false,
    @SerialName("letak_plasenta_normal") val letakPlasentaNormal: Boolean = true,
    @SerialName("usia_kehamilan_plasenta_menutupi") val usiaKehamilanPlasentaMenutupi: Int = 0,
    @SerialName("daftar_kondisi_relatif") val daftarKondisiRelatif: List<String> = emptyList(),
    @SerialName("apakah_kondisi_relatif_terkontrol") val apakahKondisiRelatifTerkontrol: Boolean = true,
    @SerialName("bb_sebelum_hamil") val bbSebelumHamil: BbSebelumHamil = BbSebelumHamil.NORMAL,
    @SerialName("frekuensi_olahraga_sebelum_hamil") val frekuensiOlahragaSebelumHamil: FrekuensiOlahraga = FrekuensiOlahraga.JARANG
)

@Serializable
data class GejalaSaatIni(
    @SerialName("kondisi_tekanan_darah") val kondisiTekananDarah: TekananDarah = TekananDarah.NORMAL,
    @SerialName("gejala_mendesak_beberapa_hari_terakhir") val gejalaMendesakBeberapaHariTerakhir: List<String> = emptyList(),
    @SerialName("gerakan_janin_aktif_hari_ini") val gerakanJaninAktifHariIni: Boolean = true,
    @SerialName("pusing_pandangan_kabur_hari_ini") val pusingPandanganKaburHariIni: Boolean = false,
    @SerialName("nyeri_tulang_kemaluan_punggung_hebat") val nyeriTulangKemaluanPunggungHebat: Boolean = false,
    @SerialName("cukup_bertenaga_hari_ini") val cukupBertenagaHariIni: Boolean = true
)

// Serializer ISO 8601 untuk Supabase
object DateSerializer : KSerializer<Date> {
    private val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US)
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(format.format(value))
    override fun deserialize(decoder: Decoder): Date = format.parse(decoder.decodeString()) ?: Date()
}
