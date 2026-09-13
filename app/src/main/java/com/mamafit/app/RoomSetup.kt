package com.mamafit.app
import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)
    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromLevelRisiko(value: LevelRisiko?): String? = value?.name
    @TypeConverter
    fun toLevelRisiko(value: String?): LevelRisiko? = value?.let { LevelRisiko.valueOf(it) }

    @TypeConverter
    fun fromTrimester(value: Trimester?): String? = value?.name
    @TypeConverter
    fun toTrimester(value: String?): Trimester? = value?.let { Trimester.valueOf(it) }

    @TypeConverter
    fun fromTekananDarah(value: TekananDarah?): String? = value?.name
    @TypeConverter
    fun toTekananDarah(value: String?): TekananDarah? = value?.let { TekananDarah.valueOf(it) }

    @TypeConverter
    fun fromJenisKehamilan(value: JenisKehamilan?): String? = value?.name
    @TypeConverter
    fun toJenisKehamilan(value: String?): JenisKehamilan? = value?.let { JenisKehamilan.valueOf(it) }

    @TypeConverter
    fun fromRiwayatPersalinan(value: RiwayatPersalinan?): String? = value?.name
    @TypeConverter
    fun toRiwayatPersalinan(value: String?): RiwayatPersalinan? = value?.let { RiwayatPersalinan.valueOf(it) }

    @TypeConverter
    fun fromBbSebelumHamil(value: BbSebelumHamil?): String? = value?.name
    @TypeConverter
    fun toBbSebelumHamil(value: String?): BbSebelumHamil? = value?.let { BbSebelumHamil.valueOf(it) }

    @TypeConverter
    fun fromFrekuensiOlahraga(value: FrekuensiOlahraga?): String? = value?.name
    @TypeConverter
    fun toFrekuensiOlahraga(value: String?): FrekuensiOlahraga? = value?.let { FrekuensiOlahraga.valueOf(it) }

    @TypeConverter
    fun fromModeLatihan(value: ModeLatihan?): String? = value?.name
    @TypeConverter
    fun toModeLatihan(value: String?): ModeLatihan? = value?.let { ModeLatihan.valueOf(it) }

    @TypeConverter
    fun fromStatusLatihan(value: StatusLatihan?): String? = value?.name
    @TypeConverter
    fun toStatusLatihan(value: String?): StatusLatihan? = value?.let { StatusLatihan.valueOf(it) }
}

@Serializable
@Entity(tableName = "hasil_skrining")
data class HasilSkriningEntity(
    @SerialName("id_skrining") @PrimaryKey val idSkrining: String,
    @SerialName("id_pengguna") val idPengguna: String,
    @SerialName("skrining_type") val skriningType: String, // Pembeda AWAL / BULANAN
    @SerialName("trimester") val trimester: Trimester,
    @Embedded val riwayatKesehatan: RiwayatKesehatan,
    @Embedded val gejalaSaatIni: GejalaSaatIni,
    @SerialName("level_risiko_sistem") val levelRisikoSistem: LevelRisiko,
    @SerialName("tanggal_pengisian") @Serializable(with = DateSerializer::class) val tanggalPengisian: Date
)

@Serializable
@Entity(tableName = "sesi_latihan")
data class SesiLatihanEntity(
    @PrimaryKey val idSesi: String,
    val idPengguna: String,
    val idGerakan: String,
    val namaGerakan: String,
    val modeLatihan: ModeLatihan,
    val statusLatihan: StatusLatihan,
    @Serializable(with = DateSerializer::class) val waktuMulai: Date,
    @Serializable(with = DateSerializer::class) val waktuSelesai: Date,
    val kaloriTerbakar: Float,
    val durasiMenit: Int,
    val skorPostur: Int,
    val masukanKecerdasanBuatan: String
)

@Dao
interface HasilSkriningDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanSkrining(skrining: HasilSkriningEntity)
    
    @Query("SELECT * FROM hasil_skrining WHERE idPengguna = :idPengguna ORDER BY tanggalPengisian DESC")
    suspend fun ambilSemuaSkrining(idPengguna: String): List<HasilSkriningEntity>
}

@Dao
interface SesiLatihanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanSesi(sesi: SesiLatihanEntity)

    @Query("SELECT * FROM sesi_latihan WHERE idPengguna = :idPengguna ORDER BY waktuSelesai DESC")
    suspend fun ambilRiwayatSesi(idPengguna: String): List<SesiLatihanEntity>
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: Pengguna)
    
    @Query("SELECT * FROM pengguna WHERE namaPengguna = :namaPengguna LIMIT 1")
    suspend fun getUserByUsername(namaPengguna: String): Pengguna?
}

@Database(entities = [HasilSkriningEntity::class, Pengguna::class, SesiLatihanEntity::class], version = 12, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MamaFitDatabase : RoomDatabase() {
    abstract fun hasilSkriningDao(): HasilSkriningDao
    abstract fun userDao(): UserDao
    abstract fun sesiLatihanDao(): SesiLatihanDao
    
    companion object {
        @Volatile private var INSTANCE: MamaFitDatabase? = null
        fun getDatabase(context: android.content.Context): MamaFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, MamaFitDatabase::class.java, "mamafit_db_v10")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
