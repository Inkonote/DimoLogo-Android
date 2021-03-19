package sh.tyy.dimo.logo

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlin.math.max
import kotlin.math.min
import android.view.Window
import android.widget.TextView


class DimoLogoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    companion object {

        private const val defaultWidth: Int = 60
        private const val defaultHeight: Int = 60
        private const val baseReference: Float = 60.0f
        private const val waterDropArcRadiusRatio: Float = 1.0f / 12.0f
        private const val waterDropControlPointOffsetYRatio: Float = 1.0f / 15.0f
        private const val waterDroStretchLengthRatio: Float = 1.0f / 15.0f
        private const val waterDropElemMaxIntervalRatio: Float = 7.0f / 30.0f
        private const val lineMarginHorizontalRatio: Float = 1.0f / 12.0f
        private const val lineWidthRatio: Float = 1.0f / 20.0f
        private const val bounceSizeRatio: Float = 1.0f / 60.0f
        private val lineStretchLengthsRatio: List<Float> = listOf(.2f, 2.0f / 15.0f, .1f)

        fun hud(context: Context, text: String? = null): Dialog {
            val contentView = LayoutInflater.from(context).inflate(R.layout.hud_dimo_logo, null)
            val logoView: DimoLogoView = contentView.findViewById(R.id.view_logo)
            val textView: TextView = contentView.findViewById(R.id.text_hud)
            textView.text = text
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(contentView)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setDimAmount(0f)
            logoView.playAnimation()
            return dialog
        }
    }

    private val line: Line = Line()
    private val waterDrop: WaterDrop = WaterDrop()

    private var animationDuration: Float = 1.2f
    var animationInterval: Float = .15f

    private var animator: ValueAnimator? = null

    private var time: Float = .0f

    var progress: Float
        set(value) {
            stopAnimation()
            val progress: Float = max(0f, min(value, 1.0f))
            time = animationDuration * progress
            invalidate()
        }
        get() {
            if (animationDuration <= 0) {
                return 0f
            }
            val timeProgress = time / animationDuration
            return max(0f, min(timeProgress, 1.0f))

        }

    var foregroundColor: Int = Color.WHITE
        set(value) {
            field = value
            if (animator == null) {
                invalidate()
            }
        }

    init {
        attrs?.let {
            val a = context.theme.obtainStyledAttributes(it, R.styleable.DimoLogoView, defStyleAttr, 0)
            if (a.hasValue(R.styleable.DimoLogoView_foregroundColor)) {
                foregroundColor = a.getColor(R.styleable.DimoLogoView_foregroundColor, Color.WHITE)
            }
        }
        updateTimes()
    }

    fun playAnimation(duration: Float = 1.2f, interval: Float = .15f) {
        stopAnimation()
        animationDuration = duration
        animationInterval = interval

        animator = ValueAnimator.ofFloat(0f, animationDuration + animationInterval)
        animator?.duration = (animationDuration * 1000).toLong()
        animator?.repeatCount = ValueAnimator.INFINITE
        animator?.addUpdateListener {
            time = it.animatedValue as Float
            invalidate()
        }
        animator?.start()
    }

    fun stopAnimation() {
        animator?.end()
        animator = null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // TODO:
        updateLayout()
        waterDrop.draw(width = width, height = height, lineWidth = line.width, time = time, canvas = canvas, foregroundColor = foregroundColor)
        line.draw(width = width, height = height, time = time, canvas = canvas, foregroundColor = foregroundColor)
    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = (defaultWidth * resources.displayMetrics.density).toInt()
        val desiredHeight = (defaultHeight * resources.displayMetrics.density).toInt()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    private fun updateLayout() {
        var reference: Float = min(width, height).toFloat()
        reference = max(reference, baseReference)

        waterDrop.arcRadius = reference * waterDropArcRadiusRatio
        waterDrop.stretchLength = reference * waterDroStretchLengthRatio
        waterDrop.bounceSize = reference * bounceSizeRatio
        waterDrop.controlPointOffsetY = reference * waterDropControlPointOffsetYRatio
        waterDrop.elemMaxInterval = reference * waterDropElemMaxIntervalRatio

        line.marginHorizontal = reference * lineMarginHorizontalRatio
        line.width = reference * lineWidthRatio
        line.stretchLengths = lineStretchLengthsRatio.map { it * reference }
    }

    private fun updateTimes() {
        waterDrop.animationTimeRange.setPop(startExpand = 0f,
                startShrink = (animationDuration * 0.125f),
                endShrink = (animationDuration * 0.15f))

        waterDrop.animationTimeRange.setMove(startStand = 0f,
                startMove = waterDrop.animationTimeRange.shrink.endInclusive,
                endMove = (animationDuration * 0.85f))

        waterDrop.animationTimeRange.setDeform(start = 0f,
                startStretch = animationDuration * 0.5f,
                endStretch = animationDuration * 0.875f)

        waterDrop.animationTimeRange.fadeIn = (animationDuration * 0.8f)..(animationDuration * 1f)

        val lineStretchTimes = listOf(animationDuration * 0.5f,
                animationDuration * 0.8f,
                animationDuration * 0.9f,
                animationDuration * 1f)

        line.stretchTimeRanges = listOf(lineStretchTimes[0]..lineStretchTimes[1],
                lineStretchTimes[1]..lineStretchTimes[2],
                lineStretchTimes[2]..lineStretchTimes[3])
    }
}