package com.mamafit.app
import androidx.room.*
import kotlinx.serialization.Serializable
import java.util.Date

// Converters
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromLevelRisiko(level: LevelRisiko?): String? = level?.name

    @TypeConverter
    fun toLevelRisiko(value: String?): LevelRisiko? = value?.let { LevelRisiko.valueOf(it) }

    @TypeConverter
    fun fromTrimester(trimester: Trimester?): String? = trimester?.name

    @TypeConverter
    fun toTrimester(value: String?): Trimester? = value?.let { Trimester.valueOf(it) }

    @TypeConverter
    fun fromLetakPlasenta(letak: LetakPlasenta?): String? = letak?.name

    @TypeConverter
    fun toLetakPlasenta(value: String?): LetakPlasenta? = value?.let { LetakPlasenta.valueOf(it) }

    @TypeConverter
    fun fromTekananDarah(tekanan: TekananDarah?): String? = tekanan?.name

    @TypeConverter
    fun toTekananDarah(value: String?): TekananDarah? = value?.let { TekananDarah.valueOf(it) }

    @TypeConverter
    fun fromGerakanJanin(gerakan: GerakanJanin?): String? = gerakan?.name

    @TypeConverter
    fun toGerakanJanin(value: String?): GerakanJanin? = value?.let { GerakanJanin.valueOf(it) }

    @TypeConverter
    fun fromStatusLatihan(status: StatusLatihan?): String? = status?.name

    @TypeConverter
    fun toStatusLatihan(value: String?): StatusLatihan? = value?.let { StatusLatihan.valueOf(it) }

    @TypeConverter
    fun fromModeLatihan(mode: ModeLatihan?): String? = mode?.name

    @TypeConverter
    fun toModeLatihan(value: String?): ModeLatihan? = value?.let { ModeLatihan.valueOf(it) }
}

// Entity
@Serializable
@Entity(tableName = "hasil_skrining")
data class HasilSkriningEntity(
    @PrimaryKey val idSkrining: String,
    val idPengguna: String,
    val periodeBulan: String,
    val trimester: Trimester,
    val beratBadanKg: Float,

    @Embedded(prefix = "riwayat_")
    val riwayatKesehatan: RiwayatKesehatan,

    @Embedded(prefix = "gejala_")
    val gejalaSaatIni: GejalaSaatIni,

    val levelRisikoSistem: LevelRisiko,
    val apakahPemeriksaanUlangBulanan: Boolean,
    @Serializable(with = DateSerializer::class)
    val tanggalPengisian: Date
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
    @Serializable(with = DateSerializer::class)
    val waktuMulai: Date,
    @Serializable(with = DateSerializer::class)
    val waktuSelesai: Date?,
    val kaloriTerbakar: Float = 0f,
    val durasiMenit: Int = 0,
    val skorPostur: Int = 0,
    val masukanKecerdasanBuatan: String? = null
)

// Dao
@Dao
interface HasilSkriningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanSkrining(skrining: HasilSkriningEntity)

    @Query("SELECT * FROM hasil_skrining WHERE idPengguna = :idPengguna ORDER BY tanggalPengisian DESC")
    suspend fun ambilSemuaSkrining(idPengguna: String): List<HasilSkriningEntity>

    @Query("SELECT * FROM hasil_skrining WHERE idPengguna = :idPengguna AND periodeBulan = :periode LIMIT 1")
    suspend fun ambilSkriningPeriodeIni(idPengguna: String, periode: String): HasilSkriningEntity?

    @Delete
    suspend fun hapusSkrining(skrining: HasilSkriningEntity)
}

@Dao
interface SesiLatihanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanSesi(sesi: SesiLatihanEntity)

    @Query("SELECT * FROM sesi_latihan WHERE idPengguna = :idPengguna ORDER BY waktuMulai DESC")
    suspend fun ambilRiwayatSesi(idPengguna: String): List<SesiLatihanEntity>

    @Query("SELECT SUM(kaloriTerbakar) FROM sesi_latihan WHERE idPengguna = :idPengguna")
    suspend fun ambilTotalKalori(idPengguna: String): Float?
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: Pengguna)

    @Query("SELECT * FROM pengguna WHERE namaPengguna = :namaPengguna LIMIT 1")
    suspend fun getUserByUsername(namaPengguna: String): Pengguna?

    @Query("SELECT * FROM pengguna WHERE namaPengguna = :namaPengguna AND kataSandi = :kataSandi LIMIT 1")
    suspend fun loginUser(namaPengguna: String, kataSandi: String): Pengguna?
}

// Database
@Database(
    entities = [HasilSkriningEntity::class, SesiLatihanEntity::class, Pengguna::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MamaFitDatabase : RoomDatabase() {

    abstract fun hasilSkriningDao(): HasilSkriningDao
    abstract fun sesiLatihanDao(): SesiLatihanDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: MamaFitDatabase? = null

        fun getDatabase(context: android.content.Context): MamaFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MamaFitDatabase::class.java,
                    "mamafit_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}