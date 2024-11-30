package com.example.launcherapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ValueAnimator
import androidx.recyclerview.widget.RecyclerView

class FastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x80FFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }

    private val letters = ('A'..'Z').toList() + ('0'..'9').toList()
    private var recyclerView: RecyclerView? = null
    private var itemHeight = 0f
    private var isScrolling = false
    private var currentLetter = ' '
    
    init {
        isHapticFeedbackEnabled = true
    }

    private val scrollAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 200
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animation ->
            alpha = animation.animatedValue as Float
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        itemHeight = h.toFloat() / letters.size
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        letters.forEachIndexed { index, char ->
            val alpha = if (isScrolling && char == currentLetter) 255 else 128
            paint.alpha = alpha
            canvas.drawText(
                char.toString(),
                width / 2f,
                itemHeight * (index + 0.75f),
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isScrolling = true
                scrollAnimator.start()
                handleScroll(event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isScrolling) {
                    handleScroll(event.y)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScrolling = false
                scrollAnimator.reverse()
                currentLetter = ' '
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleScroll(y: Float) {
        val position = ((y / height) * letters.size).toInt()
            .coerceIn(0, letters.size - 1)
        val letter = letters[position]
        if (letter != currentLetter) {
            currentLetter = letter
            scrollToPosition(position)
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            invalidate()
        }
    }

    private fun scrollToPosition(position: Int) {
        recyclerView?.let { rv ->
            val adapter = rv.adapter as? AppListAdapter
            adapter?.let { appAdapter ->
                val targetChar = letters[position]
                val targetPosition = appAdapter.getPositionForChar(targetChar)
                if (targetPosition != -1) {
                    rv.scrollToPosition(targetPosition)
                }
            }
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }
} 