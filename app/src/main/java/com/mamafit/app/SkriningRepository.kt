package com.mamafit.app

import io.github.jan.supabase.postgrest.postgrest
import java.util.Date
import java.util.UUID

class SkriningRepository(private val dao: HasilSkriningDao) {

    // Singleton sederhana untuk menyimpan jawaban selama sesi tanya jawab berlangsung
    object SkriningSession {
        private val jawaban = mutableMapOf<Int, String>()
        
        fun simpanJawaban(index: Int, optionId: String) {
            jawaban[index] = optionId
        }

        fun ambilSemuaJawaban(): Map<Int, String> = jawaban

        fun reset() {
            jawaban.clear()
        }
    }

    /**
     * Logika utama untuk menentukan LevelRisiko berdasarkan aturan medis sederhana
     */
    fun hitungLevelRisiko(jawaban: Map<Int, String>): LevelRisiko {
        // 1. Cek Kategori TINGGI (Kondisi Bahaya)
        val jantung = jawaban[1] == "ya"      // Q2
        val pendarahan = jawaban[3] == "ya"   // Q4
        val nyeriPerut = jawaban[4] == "ya"   // Q5
        val pusing = jawaban[7] == "ya"       // Q8

        if (jantung || pendarahan || nyeriPerut || pusing) {
            return LevelRisiko.TINGGI
        }

        // 2. Cek Kategori SEDANG (Kondisi Waspada)
        val tekanan = jawaban[2]              // Q3: normal, rendah, tinggi
        val plasenta = jawaban[5]             // Q6: normal, previa, tidak_tahu
        val gerakan = jawaban[6]              // Q7: aktif, kurang_aktif, belum_terasa

        if (tekanan != "normal" || plasenta == "previa" || gerakan == "kurang_aktif") {
            return LevelRisiko.SEDANG
        }

        // 3. Default: RENDAH (Aman)
        return LevelRisiko.RENDAH
    }

    /**
     * Menyimpan hasil akhir ke Database Room
     */
    suspend fun simpanHasilSkrining(idPengguna: String): LevelRisiko {
        val jawabanMap = SkriningSession.ambilSemuaJawaban()
        val level = hitungLevelRisiko(jawabanMap)
        
        // Mapping dari Map ID ke objek data class
        val riwayat = RiwayatKesehatan(
            penyakitJantung = jawabanMap[1] == "ya",
            letakPlasenta = when(jawabanMap[5]) {
                "previa" -> LetakPlasenta.PREVIA
                "tidak_tahu" -> LetakPlasenta.TIDAK_TAHU
                else -> LetakPlasenta.NORMAL
            }
        )

        val gejala = GejalaSaatIni(
            tekananDarah = when(jawabanMap[2]) {
                "rendah" -> TekananDarah.RENDAH
                "tinggi" -> TekananDarah.TINGGI
                else -> TekananDarah.NORMAL
            },
            pendarahan = jawabanMap[3] == "ya",
            nyeriPerutHebat = jawabanMap[4] == "ya",
            pusingBerat = jawabanMap[7] == "ya",
            gerakanJanin = when(jawabanMap[6]) {
                "kurang_aktif" -> GerakanJanin.KURANG_AKTIF
                "belum_terasa" -> GerakanJanin.BELUM_TERASA
                else -> GerakanJanin.AKTIF
            },
            nyeriTulangKemaluan = jawabanMap[8] == "ya",
            bertenaga = jawabanMap[9] == "sangat_siap" || jawabanMap[9] == "cukup"
        )

        val entity = HasilSkriningEntity(
            idSkrining = UUID.randomUUID().toString(),
            idPengguna = idPengguna,
            periodeBulan = "Agustus 2026", // Bisa dibuat dinamis nantinya
            trimester = when(jawabanMap[0]) {
                "t1" -> Trimester.SATU
                "t2" -> Trimester.DUA
                else -> Trimester.TIGA
            },
            beratBadanKg = 0f, // Default karena belum ada input BB di skrining
            riwayatKesehatan = riwayat,
            gejalaSaatIni = gejala,
            levelRisikoSistem = level,
            apakahPemeriksaanUlangBulanan = false,
            tanggalPengisian = Date()
        )

        // Simpan ke database Lokal
        dao.simpanSkrining(entity)

        // Simpan ke Supabase Cloud
        try {
            val supabase = SupabaseManager.client
            supabase.postgrest["hasil_skrining"].insert(entity)
        } catch (e: Exception) {
            android.util.Log.e("MamaFit", "Supabase Sync Error: ${e.message}")
        }
        
        SkriningSession.reset() // Bersihkan sesi setelah berhasil simpan
        return level
    }
}
