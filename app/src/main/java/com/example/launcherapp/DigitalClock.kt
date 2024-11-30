package com.example.launcherapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class DigitalClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 72f
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var currentTime = ""

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            postDelayed(this, 1000)
        }
    }

    private fun updateTime() {
        currentTime = timeFormat.format(Date())
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
        canvas.drawText(currentTime, width / 2f, height * 0.65f, timePaint)
    }
} 