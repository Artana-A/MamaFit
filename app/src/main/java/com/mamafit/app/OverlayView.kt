package com.mamafit.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    private fun initPaints() {
        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = 8f
        pointPaint.style = Paint.Style.FILL

        linePaint.color = Color.GREEN
        linePaint.strokeWidth = 5f
        linePaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * width * scaleFactor,
                        normalizedLandmark.y() * height * scaleFactor,
                        pointPaint
                    )
                }
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int
    ) {
        this.results = poseLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        // Kalkulasi skala agar pas di layar
        scaleFactor = Math.max(width * 1f / imageWidth, height * 1f / imageHeight)
        invalidate()
    }
}
