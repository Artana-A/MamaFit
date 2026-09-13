package com.mamafit.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.mamafit.app.databinding.ActivitySesiOlahragaBinding
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SesiOlahragaActivity : AppCompatActivity(), PoseDetectorHelper.DetectorListener {

    private lateinit var binding: ActivitySesiOlahragaBinding
    private var poseDetectorHelper: PoseDetectorHelper? = null
    private lateinit var cameraExecutor: ExecutorService
    
    private var exerciseId: String = ""
    private var exerciseName: String = ""
    private var repCount = 0
    private var startTime: Long = 0
    private var isDangerStopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySesiOlahragaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exerciseId = intent.getStringExtra("EXTRA_EXERCISE_ID") ?: "G001"
        exerciseName = intent.getStringExtra("EXTRA_EXERCISE_NAME") ?: "Latihan Panggul"
        binding.tvExerciseName.text = exerciseName

        cameraExecutor = Executors.newSingleThreadExecutor()
        startTime = System.currentTimeMillis()

        // 1. Panduan Keamanan Awal
        showSafetyDialog()

        binding.btnDanger.setOnClickListener {
            handleDangerSign()
        }

        binding.btnMulaiKamera.setOnClickListener {
            checkPermissionsAndStart()
        }
    }

    private fun showSafetyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Panduan Keamanan AI")
            .setMessage("Pastikan area sekitar Anda luas. Jika Bunda merasa pusing, sesak napas, atau nyeri, segera hentikan latihan dengan menekan tombol bahaya.")
            .setPositiveButton("Saya Mengerti") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun startCamera() {
        binding.cameraIdleState.visibility = View.GONE
        binding.cameraActiveState.visibility = View.VISIBLE
        binding.viewFinder.visibility = View.VISIBLE
        binding.overlayView.visibility = View.VISIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            poseDetectorHelper = PoseDetectorHelper(this, this)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        val bitmap = BitmapImageBuilder(image.toBitmap()).build()
                        poseDetectorHelper?.detectLiveStream(bitmap)
                        image.close()
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis)
            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal memulai kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResults(result: PoseLandmarkerResult, inferenceTime: Long, imageHeight: Int, imageWidth: Int) {
        runOnUiThread {
            binding.overlayView.setResults(result, imageHeight, imageWidth)
            if (result.landmarks().isNotEmpty()) {
                analyzePosture(result)
            }
        }
    }

    private fun analyzePosture(result: PoseLandmarkerResult) {
        // AI Feedback logic here
        binding.tvAiFeedback.text = "Postur Bagus! Teruskan."
        
        // Simulasi hitung reps sederhana (Logic asli akan membandingkan landmark panggul/lutut)
        // repCount++
        // binding.tvRepsCount.text = repCount.toString()
        
        // Jika sudah mencapai target (misal 10), otomatis selesai
        // if (repCount >= 10) finishSession(StatusLatihan.SELESAI)
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDangerSign() {
        isDangerStopped = true
        AlertDialog.Builder(this)
            .setTitle("Latihan Dihentikan")
            .setMessage("Bunda merasakan keluhan bahaya. Silakan beristirahat dan konsultasikan dengan bidan jika nyeri berlanjut.")
            .setPositiveButton("Hubungi Bidan") { _, _ ->
                finishSession(StatusLatihan.DIHENTIKAN_KARENA_BAHAYA)
                val intent = Intent(Intent.ACTION_DIAL)
                // intent.data = Uri.parse("tel:08123456789")
                startActivity(intent)
            }
            .setNegativeButton("Istirahat") { _, _ ->
                finishSession(StatusLatihan.DIHENTIKAN_KARENA_BAHAYA)
            }
            .setCancelable(false)
            .show()
    }

    private fun finishSession(status: StatusLatihan) {
        val duration = ((System.currentTimeMillis() - startTime) / 60000).toInt()
        val kalori = duration * 5f
        val skor = if (isDangerStopped) 0 else 85

        lifecycleScope.launch {
            val db = MamaFitDatabase.getDatabase(this@SesiOlahragaActivity)
            val prefs = getSharedPreferences("mamafit_prefs", MODE_PRIVATE)
            val userId = prefs.getString("user_id", "00000000-0000-0000-0000-000000000000") ?: "00000000-0000-0000-0000-000000000000"

            val sesi = SesiLatihanEntity(
                idSesi = UUID.randomUUID().toString(),
                idPengguna = userId,
                idGerakan = exerciseId,
                namaGerakan = exerciseName,
                modeLatihan = ModeLatihan.GERAKAN_AKTIF,
                statusLatihan = status,
                waktuMulai = Date(startTime),
                waktuSelesai = Date(),
                kaloriTerbakar = kalori,
                durasiMenit = duration,
                skorPostur = skor,
                masukanKecerdasanBuatan = if (isDangerStopped) "Latihan dihentikan demi keamanan." else "Latihan luar biasa, postur Bunda stabil!"
            )
            db.sesiLatihanDao().simpanSesi(sesi)
            
            // Sync ke Supabase dengan buildJsonObject agar aman (lowercase enum & UUID format)
            try {
                val supabaseSesi = buildJsonObject {
                    put("id_sesi", sesi.idSesi)
                    put("id_pengguna", sesi.idPengguna)
                    put("id_gerakan", sesi.idGerakan)
                    put("nama_gerakan", sesi.namaGerakan)
                    put("mode_latihan", sesi.modeLatihan.name.lowercase())
                    put("status_latihan", sesi.statusLatihan.name.lowercase())
                    put("waktu_mulai", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US).format(sesi.waktuMulai))
                    put("waktu_selesai", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US).format(sesi.waktuSelesai))
                    put("kalori_terbakar", sesi.kaloriTerbakar)
                    put("durasi_menit", sesi.durasiMenit)
                    put("skor_postur", sesi.skorPostur)
                    put("masukan_kecerdasan_buatan", sesi.masukanKecerdasanBuatan)
                }
                SupabaseManager.client.postgrest["sesi_latihan"].insert(supabaseSesi)
            } catch (e: Exception) {
                android.util.Log.e("MamaFit", "Supabase Sesi Sync Error: ${e.message}")
            }
            
            val intent = Intent(this@SesiOlahragaActivity, SkorSelesaiActivity::class.java)
            intent.putExtra("EXTRA_SESI_ID", sesi.idSesi)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
