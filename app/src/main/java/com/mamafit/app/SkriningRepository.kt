package com.mamafit.app

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Date
import java.util.UUID

class SkriningRepository(private val dao: HasilSkriningDao) {

    object SkriningSession {
        private val jawaban = mutableMapOf<String, String>()
        var type: SkriningType = SkriningType.AWAL
        
        fun simpanJawaban(questionId: String, value: String) {
            jawaban[questionId] = value
        }
        fun ambilJawaban(questionId: String): String? = jawaban[questionId]
        fun ambilSemuaJawaban(): Map<String, String> = jawaban
        fun reset() {
            jawaban.clear()
            type = SkriningType.AWAL
        }
    }

    fun hitungLevelRisiko(jawaban: Map<String, String>): LevelRisiko {
        val hasAbsolut = jawaban["q4"] == "ya" || jawaban["q5"] == "ya" || jawaban["q6"] == "ya" || 
                        jawaban["q7"] == "tinggi_baru" || jawaban["q8"] == "menutupi" || 
                        (jawaban["q9"] != null && jawaban["q9"] != "tidak_ada" && jawaban["q9"] != "")

        val hasRelatif = jawaban["q10"] != null && jawaban["q10"] != "tidak_ada" && jawaban["q10"] != ""
        val isRelatifTerkontrol = jawaban["q11"] == "terkontrol"

        return when {
            hasAbsolut || (hasRelatif && !isRelatifTerkontrol) -> LevelRisiko.TINGGI
            hasRelatif && isRelatifTerkontrol -> LevelRisiko.SEDANG
            else -> LevelRisiko.RENDAH
        }
    }

    suspend fun simpanHasilSkrining(idPengguna: String): LevelRisiko {
        val jawabanMap = SkriningSession.ambilSemuaJawaban()
        val level = hitungLevelRisiko(jawabanMap)
        
        val riwayat = RiwayatKesehatan(
            jenisKehamilan = if (jawabanMap["q2"] == "kembar") JenisKehamilan.KEMBAR else JenisKehamilan.TUNGGAL,
            riwayatPersalinan = when(jawabanMap["q3"]) {
                "normal" -> RiwayatPersalinan.NORMAL
                "caesar" -> RiwayatPersalinan.SESAR
                else -> RiwayatPersalinan.BELUM_PERNAH
            },
            riwayatJantung = jawabanMap["q4"] == "ya",
            penyakitParuBerat = jawabanMap["q5"] == "ya",
            inkompetensiServiks = jawabanMap["q6"] == "ya",
            letakPlasentaNormal = jawabanMap["q8"] == "normal",
            usiaKehamilanPlasentaMenutupi = 0,
            daftarKondisiRelatif = jawabanMap["q10"]?.split(";")?.filter { it.isNotEmpty() } ?: emptyList(),
            apakahKondisiRelatifTerkontrol = jawabanMap["q11"] == "terkontrol",
            bbSebelumHamil = when(jawabanMap["q12"]) {
                "kurus" -> BbSebelumHamil.SANGAT_KURUS
                "gemuk" -> BbSebelumHamil.GEMUK_OBESITAS
                else -> BbSebelumHamil.NORMAL
            },
            frekuensiOlahragaSebelumHamil = when(jawabanMap["q13"]) {
                "rutin" -> FrekuensiOlahraga.RUTIN
                "jarang" -> FrekuensiOlahraga.JARANG
                else -> FrekuensiOlahraga.TIDAK_PERNAH
            }
        )

        val gejala = GejalaSaatIni(
            kondisiTekananDarah = when(jawabanMap["q7"]) {
                "tinggi_baru" -> TekananDarah.HIPERTENSI_GESTASIONAL
                "tinggi_lama" -> TekananDarah.KRONIS
                else -> TekananDarah.NORMAL
            },
            gejalaMendesakBeberapaHariTerakhir = jawabanMap["q9"]?.split(";")?.filter { it.isNotEmpty() } ?: emptyList(),
            gerakanJaninAktifHariIni = jawabanMap["q14"] == "aktif",
            pusingPandanganKaburHariIni = jawabanMap["q15"] == "ya",
            nyeriTulangKemaluanPunggungHebat = jawabanMap["q16"] == "ya",
            cukupBertenagaHariIni = jawabanMap["q17"] == "sangat_siap" || jawabanMap["q17"] == "cukup"
        )

        val entity = HasilSkriningEntity(
            idSkrining = UUID.randomUUID().toString(),
            idPengguna = idPengguna,
            skriningType = SkriningSession.type.name,
            trimester = when(jawabanMap["q1"]) {
                "t1" -> Trimester.SATU
                "t2" -> Trimester.DUA
                else -> Trimester.TIGA
            },
            riwayatKesehatan = riwayat,
            gejalaSaatIni = gejala,
            levelRisikoSistem = level,
            tanggalPengisian = Date()
        )

        dao.simpanSkrining(entity)

        try {
            // Gunakan buildJsonObject agar Supabase-kt bisa men-serialisasi data dengan benar
            val supabaseData = buildJsonObject {
                put("id_skrining", entity.idSkrining)
                put("id_pengguna", entity.idPengguna)
                put("skrining_type", entity.skriningType.lowercase()) // Kirim tipe skrining
                put("trimester", when(entity.trimester) {
                    Trimester.SATU -> "1"
                    Trimester.DUA -> "2"
                    Trimester.TIGA -> "3"
                })
                put("jenis_kehamilan", entity.riwayatKesehatan.jenisKehamilan.name.lowercase())
                put("riwayat_persalinan", entity.riwayatKesehatan.riwayatPersalinan.name.lowercase())
                put("riwayat_jantung", entity.riwayatKesehatan.riwayatJantung)
                put("penyakit_paru_berat", entity.riwayatKesehatan.penyakitParuBerat)
                put("inkompetensi_serviks", entity.riwayatKesehatan.inkompetensiServiks)
                put("letak_plasenta_normal", entity.riwayatKesehatan.letakPlasentaNormal)
                put("usia_kehamilan_plasenta_menutupi", entity.riwayatKesehatan.usiaKehamilanPlasentaMenutupi)
                put("apakah_kondisi_relatif_terkontrol", entity.riwayatKesehatan.apakahKondisiRelatifTerkontrol)
                put("bb_sebelum_hamil", entity.riwayatKesehatan.bbSebelumHamil.name.lowercase())
                put("frekuensi_olahraga_sebelum_hamil", entity.riwayatKesehatan.frekuensiOlahragaSebelumHamil.name.lowercase())
                put("kondisi_tekanan_darah", entity.gejalaSaatIni.kondisiTekananDarah.name.lowercase())
                put("gerakan_janin_aktif_hari_ini", entity.gejalaSaatIni.gerakanJaninAktifHariIni)
                put("pusing_pandangan_kabur_hari_ini", entity.gejalaSaatIni.pusingPandanganKaburHariIni)
                put("nyeri_tulang_kemaluan_punggung_hebat", entity.gejalaSaatIni.nyeriTulangKemaluanPunggungHebat)
                put("cukup_bertenaga_hari_ini", entity.gejalaSaatIni.cukupBertenagaHariIni)
                put("level_risiko_sistem", entity.levelRisikoSistem.name.lowercase())
                put("tanggal_pengisian", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US).format(entity.tanggalPengisian))
            }
            
            SupabaseManager.client.postgrest["hasil_skrining"].insert(supabaseData)
        } catch (e: Exception) {
            android.util.Log.e("MamaFit", "Supabase Sync Error: ${e.message}")
        }
        
        SkriningSession.reset()
        return level
    }
}
