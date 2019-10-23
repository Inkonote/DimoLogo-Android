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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.view.Window
import android.widget.TextView


class DimoLogoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    companion object {

        private const val defaultWidth: Int = 60
        private const val defaultHeight: Int = 60
        private const val BaseReference: Float = 60.0f
        private const val WaterDropArcRadiusRatio: Float = 1.0f / 12.0f
        private const val WaterDropControlPointOffsetYRatio: Float = 1.0f / 15.0f
        private const val WaterDroStretchLengthRatio: Float = 1.0f / 15.0f
        private const val WaterDropElemMaxIntervalRatio: Float = 7.0f / 30.0f
        private const val LineMarginHorizontalRatio: Float = 1.0f / 12.0f
        private const val LineWidthRatio: Float = 1.0f / 20.0f
        private const val BounceSizeRatio: Float = 1.0f / 60.0f
        private val LineStretchLengthsRatio: List<Float> = listOf(.2f, 2.0f / 15.0f, .1f)

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

    private var waterDropArcRadius: Float = 5.0f
    private var waterDropControlPointOffsetY: Float = 4.0f
    private var waterDroStretchLength: Float = 4.0f
    private var waterDropElemMaxInterval: Float = 14.0f
    private var lineMarginHorizontal: Float = 5.0f
    private var lineWidth: Float = 3.0f
    private var bounceSize: Float = 1.0f
    private var lineStretchLengths: List<Float> = listOf(12.0f, -8.0f, 6.0f)

    private var popAnimationFinishedTime: Float = .0f
    private var repopAnimationFinishedTime: Float = .0f
    private var moveAnimationFinishedTime: Float = .0f
    private var waterDropStretchBeginTime: Float = .0f
    private var waterDropStretchFinishedTime: Float = .0f
    private var fadeInBeginTime: Float = .0f
    private var fadeInFinishedTime: Float = .0f

    private var lineStretchBeginTime: Float = .0f
    private var lineStretchFinishedTime: Float = .0f
    private var lineStretch2FinishedTime: Float = .0f
    private var lineStretch3FinishedTime: Float = .0f

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
        drawWaterDrop(time, canvas)
        drawLine(time, canvas)
    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = (defaultWidth * resources.displayMetrics.density).toInt()
        val desiredHeight = (defaultHeight * resources.displayMetrics.density).toInt()

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)


        val width: Int = when (widthMode) {
            View.MeasureSpec.EXACTLY -> widthSize
            View.MeasureSpec.AT_MOST -> Math.min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height: Int = when (heightMode) {
            View.MeasureSpec.EXACTLY -> heightSize
            View.MeasureSpec.AT_MOST -> Math.min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    private fun drawWaterDrop(time: Float, canvas: Canvas?) {
        val arcCenterX = width / 2.0f
        var arcCenterY = height / 2.0f - waterDropElemMaxInterval - (waterDropArcRadius + lineWidth) / 2.0f
        val popRange = 0.0f..popAnimationFinishedTime
        val repopRange = popAnimationFinishedTime..repopAnimationFinishedTime
        val noMoveRange = 0.0f..repopAnimationFinishedTime
        val moveRange = repopAnimationFinishedTime..moveAnimationFinishedTime
        val noStretchRange = 0.0f..waterDropStretchBeginTime
        val stretchRange = waterDropStretchBeginTime..waterDropStretchFinishedTime
        val alphaRange = fadeInBeginTime..fadeInFinishedTime
        var alpha: Float = 1f

        // move
        if (!noMoveRange.contains(time)) {
            val elapsed = time - moveRange.start
            val distance: Float = 2f * (height / 2.0f - arcCenterY)
            arcCenterY += distance * min(1f, elapsed / moveRange.length())
        }

        var radius = waterDropArcRadius

        if (popRange.contains(time)) {
            val elapsed = time - popRange.start
            radius = (waterDropArcRadius + bounceSize) * elapsed / popRange.length()
        } else if (repopRange.contains(time)) {
            val elapsed = time - repopRange.start
            radius = waterDropArcRadius + bounceSize * (1 - elapsed / repopRange.length())
        }

        if (noStretchRange.contains(time)) {
            val paint: Paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = foregroundColor
            canvas?.drawCircle(arcCenterX, arcCenterY, radius, paint)
        } else {

            if (time >= alphaRange.start) {
                val elapsed = time - alphaRange.start
                alpha = 1 - elapsed / alphaRange.length()
            }

            val elapsed = time - stretchRange.start
            val path = waterDropPath(min(1f, elapsed / stretchRange.length()), arcCenterX, arcCenterY)
            val paint: Paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = foregroundColor
            paint.alpha = max((alpha * 255).toInt(), 0)
            canvas?.drawPath(path, paint)
        }
    }


    private fun updateLayout() {
        var reference: Float = min(width, height).toFloat()
        reference = max(reference, BaseReference)

        waterDropArcRadius = reference * DimoLogoView.WaterDropArcRadiusRatio

        waterDroStretchLength = reference * DimoLogoView.WaterDroStretchLengthRatio

        lineMarginHorizontal = reference * DimoLogoView.LineMarginHorizontalRatio

        lineWidth = reference * DimoLogoView.LineWidthRatio

        bounceSize = reference * DimoLogoView.BounceSizeRatio

        lineStretchLengths = DimoLogoView.LineStretchLengthsRatio.map { it * reference }

        waterDropControlPointOffsetY = reference * DimoLogoView.WaterDropControlPointOffsetYRatio

        waterDropElemMaxInterval = reference * DimoLogoView.WaterDropElemMaxIntervalRatio
    }

    private fun drawLine(time: Float, canvas: Canvas?) {
        val boundsWidth = width
        val centerX = boundsWidth / 2f
        val centerY = height / 2f
        val lineLength = boundsWidth - 2 * lineMarginHorizontal
        val startPoint = Pair<Float, Float>(centerX - lineLength / 2f, centerY)
        val endPoint = Pair<Float, Float>(centerX + lineLength / 2f, centerY)
        val maxOffsets: List<Float> = lineStretchLengths
        val centerPointX = centerX
        var centerPointY = centerY

        val stretchRanges = listOf(lineStretchBeginTime..lineStretchFinishedTime,
                lineStretchFinishedTime..lineStretch2FinishedTime,
                lineStretch2FinishedTime..lineStretch3FinishedTime)
        for (i in 0 until 3) {

            val range = stretchRanges[i]
            val maxOffset = maxOffsets[i]
            if (range.contains(time)) {
                val elapsed = time - range.start
                var process = elapsed / range.length()
                if (process > .5) {
                    process = abs(1 - process)
                }
                centerPointY += maxOffset * process
                break
            }
        }

        val linePath = Path()
        val lineWith2: Float = lineWidth / 2f

        val leftTop = Pair(startPoint.first, startPoint.second - lineWith2)
        val centerTop = Pair(centerPointX, centerPointY - lineWith2)
        val rightTop = Pair(endPoint.first, endPoint.second - lineWith2)

        val leftBottom = Pair(startPoint.first, startPoint.second + lineWith2)
        val centerBottom = Pair(centerPointX, centerPointY + lineWith2)
        val rightBottom = Pair(endPoint.first, endPoint.second + lineWith2)

        linePath.moveTo(leftTop)
        linePath.quadTo(leftTop.first + 16f, leftTop.second, centerTop)
        linePath.quadTo(rightTop.first - 16f, rightTop.second, rightTop)
        linePath.lineTo(rightBottom)
        linePath.quadTo(rightBottom.first - 16f, rightBottom.second, centerBottom)
        linePath.quadTo(leftBottom.first + 16f, leftBottom.second, leftBottom)
        linePath.lineTo(leftTop)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = foregroundColor
        canvas?.drawPath(linePath, paint)
    }

    private fun waterDropPath(process: Float, arcCenterX: Float, arcCenterY: Float): Path {
        val waterDropTop = Pair(arcCenterX, arcCenterY - waterDropArcRadius - waterDroStretchLength * process)
        val path = Path()
        path.moveTo(waterDropTop)
        path.quadTo(arcCenterX + waterDropArcRadius, arcCenterY - waterDropControlPointOffsetY, arcCenterX + waterDropArcRadius, arcCenterY)
        val arcLeft = arcCenterX - waterDropArcRadius
        val arcTop = arcCenterY - waterDropArcRadius
        path.addArc(RectF(arcLeft, arcTop, arcLeft + 2 * waterDropArcRadius, arcTop + 2 * waterDropArcRadius), 0f, 180f)
        path.quadTo(arcCenterX - waterDropArcRadius, arcCenterY - waterDropControlPointOffsetY, waterDropTop)

        return path
    }

    private fun updateTimes() {
        popAnimationFinishedTime = animationDuration * .125f
        repopAnimationFinishedTime = animationDuration * .15f
        moveAnimationFinishedTime = animationDuration * .85f
        waterDropStretchBeginTime = animationDuration * .5f
        waterDropStretchFinishedTime = animationDuration * .875f
        fadeInBeginTime = animationDuration * .8f
        fadeInFinishedTime = animationDuration * 1f

        lineStretchBeginTime = animationDuration * .5f
        lineStretchFinishedTime = animationDuration * .8f
        lineStretch2FinishedTime = animationDuration * .9f
        lineStretch3FinishedTime = animationDuration * 1f
    }
}