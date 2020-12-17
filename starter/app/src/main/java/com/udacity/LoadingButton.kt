package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.properties.Delegates


private const val STROKE_WIDTH = 2f

class LoadingButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val PERCENTAGE_MAX = 100
        const val ANIMATE_DURATION = 100L
    }

    private var widthSize = 0
    private var heightSize = 0

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            is ButtonState.Clicked -> {
                isClickable = false
                buttonState = ButtonState.Loading
                _lastPercentage = 0
            }
            is ButtonState.Loading -> {
                text = context.getString(R.string.button_loading)
            }
            is ButtonState.Completed -> {
                text = context.getString(R.string.download)
                isClickable = true
            }
        }
    }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, context.resources.displayMetrics)
        strokeWidth = STROKE_WIDTH * resources.displayMetrics.density + 0.5f
        color = context.getColor(R.color.colorPrimary)
    }

    private var text: String
    private var _progress = 0

    private var _lastPercentage: Int
    private var _currentPercentage = 0

    private val ovalRect = RectF()

    init {
        text = context.getString(R.string.download)
        _lastPercentage = 0
        isClickable = true
    }

    // to update the progress of Loading Button
    fun updateProgress(progress: Int) {
        _progress = when {
            progress > PERCENTAGE_MAX -> PERCENTAGE_MAX
            else -> progress
        }
        animateProgress(progress.toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawBackground(it)
            if (buttonState == ButtonState.Loading) {
                drawProgress(it)
                drawCircle(it)
            }
            drawText(it)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        paint.color = context.getColor(R.color.colorPrimary)
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun drawProgress(canvas: Canvas) {
        paint.color = context.getColor(R.color.colorPrimaryDark)
        val progressWidth = (_currentPercentage.toFloat()/ PERCENTAGE_MAX) * widthSize
        canvas.drawRect(0f, 0f, progressWidth, heightSize.toFloat(), paint)
    }

    private fun drawText(canvas: Canvas) {
        paint.color = context.getColor(R.color.white)
        // Center text vertically
        val verticalCenter = (height / 2) - ((paint.descent() + paint.ascent()) / 2)
        canvas.drawText(text, widthSize.toFloat() / 2, verticalCenter, paint)
    }

    private fun drawCircle(canvas: Canvas) {
        val ovalSize = heightSize / 6
        val verticalCenter = (heightSize.div(2)).toFloat()
        val textMeasure = (paint.measureText(text) / 2) + widthSize.div(2)

        ovalRect.set(
            textMeasure + ovalSize - ovalSize,
            verticalCenter - ovalSize,
            textMeasure + ovalSize + ovalSize,
            verticalCenter + ovalSize
        )
        val percentageToFill = 360 * _currentPercentage.toFloat() / PERCENTAGE_MAX
        paint.color = context.getColor(R.color.colorAccent)
        canvas.drawArc(ovalRect, 0f, percentageToFill, true, paint  )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
                MeasureSpec.getSize(w),
                heightMeasureSpec,
                0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    /*
    * Animate the progress to make it smooth
    * */
    private fun animateProgress(progress: Float) {
        val process = if (progress >= 0) progress else _currentPercentage.toFloat() + 1

        val animator = ValueAnimator.ofFloat(_lastPercentage.toFloat(), process)
        animator.apply {
            duration = ANIMATE_DURATION
            addUpdateListener {
                _currentPercentage = (it.animatedValue as Float).toInt()
                invalidate()
            }
            doOnEnd {
                _lastPercentage = process.toInt()
            }
        }
        animator.start()
    }

    /**
    * To mark the progress is complete
    * */
    fun complete() {
        buttonState = ButtonState.Completed
    }

    // When button is clicked
    override fun performClick(): Boolean {
        if (buttonState == ButtonState.Completed)
            buttonState = ButtonState.Clicked
        return super.performClick()
    }
}