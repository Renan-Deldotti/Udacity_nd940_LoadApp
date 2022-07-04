package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var defaultBackgroundColor = getContext().getColor(R.color.colorPrimaryDark)
    private var progressBackgroundColor = getContext().getColor(R.color.colorPrimary)
    private var buttonIconColor = getContext().getColor(R.color.colorAccent)
    private var buttonTextColor = getContext().getColor(R.color.white)
    private var buttonTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20.0f, Resources.getSystem().displayMetrics)
    private val eightDpMargin =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0f, Resources.getSystem().displayMetrics)
    private val defaultIconSize =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0f, Resources.getSystem().displayMetrics)
    private var currentStateString = getContext().getString(R.string.ready_to_download_state)

    private var currentProgress = 0.0f

    private val defaultButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = defaultBackgroundColor
    }

    private val progressButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = progressBackgroundColor
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = buttonIconColor
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = buttonTextColor
        textSize = buttonTextSize
        textAlign = Paint.Align.CENTER
    }

    private var valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Completed -> {
                // If the state is "Completed" we can cancel the animation
                currentStateString = getContext().getString(R.string.ready_to_download_state)
                stopAnimations()
            }
            ButtonState.Loading -> {
                // Start to load
                currentStateString = getContext().getString(R.string.downloading_state)
                startAnimations()
            }
            else -> {}
        }
    }

    init {
        buttonState = ButtonState.Clicked
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(0.0f,0.0f, widthSize.toFloat(), heightSize.toFloat(), defaultButtonPaint)

        if (buttonState == ButtonState.Loading) {
            canvas?.drawRect(0.0f, 0.0f, currentProgress * widthSize.toFloat(), heightSize.toFloat(), progressButtonPaint)

            val left = (widthSize / 2.0f  + eightDpMargin) * 1.35f
            val top = heightSize / 2.0f - defaultIconSize
            val right = left + defaultIconSize * 2.0f
            val bottom = top + defaultIconSize * 2.0f
            val currentIconProgress = currentProgress * 360.0f
            canvas?.drawArc(left, top, right, bottom, 0.0f, currentIconProgress, true, iconPaint)
        }
        canvas?.drawText(currentStateString, widthSize / 2.0f, ((heightSize / 2.0f) * 1.2).toFloat(), textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun startAnimations() {
        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).also {
            it.duration = 300 * 5
            it.repeatMode = ValueAnimator.REVERSE
            it.repeatCount = ValueAnimator.INFINITE
            it.addUpdateListener { vA ->
                currentProgress = vA.animatedValue as Float
                invalidate()
            }
            it.disableViewDuringAnimation(this)
        }
        valueAnimator.start()
    }

    private fun stopAnimations() {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        currentProgress = 0.0f
        invalidate()
    }

    // Disable touching the button while downloading
    private fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }
}