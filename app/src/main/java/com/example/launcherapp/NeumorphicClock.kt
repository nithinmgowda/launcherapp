package com.example.launcherapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class NeumorphicClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 80f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x80FFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 14f
    }

    private var hours = ""
    private var minutes = ""
    private var seconds = ""

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            postDelayed(this, 1000)
        }
    }

    private fun updateTime() {
        val calendar = Calendar.getInstance()
        hours = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        minutes = String.format("%02d", calendar.get(Calendar.MINUTE))
        seconds = String.format("%02d", calendar.get(Calendar.SECOND))
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(updateTimeRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(updateTimeRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val y = height / 2f

        // Draw time
        val timeSpacing = timePaint.textSize * 1.5f
        canvas.drawText(hours, centerX - timeSpacing, y, timePaint)
        canvas.drawText(":", centerX, y, timePaint)
        canvas.drawText(minutes, centerX + timeSpacing, y, timePaint)
        canvas.drawText(seconds, centerX + timeSpacing * 2, y, timePaint)

        // Draw labels
        val labelY = y + timePaint.textSize / 2 + 20
        canvas.drawText("HOURS", centerX - timeSpacing, labelY, labelPaint)
        canvas.drawText("MINUTES", centerX + timeSpacing, labelY, labelPaint)
        canvas.drawText("SECONDS", centerX + timeSpacing * 2, labelY, labelPaint)
    }
} 