package com.mamafit.app

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseDetectorHelper(
    val context: Context,
    val listener: DetectorListener
) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("pose_landmarker_lite.task")
        
        try {
            val baseOptions = baseOptionsBuilder.build()
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::returnLivestreamResult)
                .setErrorListener(this::returnLivestreamError)
            
            val options = optionsBuilder.build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            listener.onError("Pose landmarker failed to initialize. See error logs for details")
            Log.e("MamaFit", "MP Error: " + e.message)
        }
    }

    fun detectLiveStream(image: MPImage) {
        val frameTime = SystemClock.uptimeMillis()
        poseLandmarker?.detectAsync(image, frameTime)
    }

    private fun returnLivestreamResult(
        result: PoseLandmarkerResult,
        input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        listener.onResults(
            result,
            inferenceTime,
            input.height,
            input.width
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        listener.onError(error.message ?: "An unknown error has occurred")
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            result: PoseLandmarkerResult,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int
        )
    }
}
