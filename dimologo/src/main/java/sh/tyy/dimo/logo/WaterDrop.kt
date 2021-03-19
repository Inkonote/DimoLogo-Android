package sh.tyy.dimo.logo

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

internal class WaterDrop {
    class AnimationTimeRange {
        var fadeIn: ClosedFloatingPointRange<Float> = 0f..0f

        var expand: ClosedFloatingPointRange<Float> = 0f..0f
            private set

        var shrink: ClosedFloatingPointRange<Float> = 0f..0f
            private set

        var stand: ClosedFloatingPointRange<Float> = 0f..0f
            private set
        var move: ClosedFloatingPointRange<Float> = 0f..0f
            private set

        var round: ClosedFloatingPointRange<Float> = 0f..0f
            private set

        var stretch: ClosedFloatingPointRange<Float> = 0f..0f
            private set

        fun setPop(startExpand: Float, startShrink: Float, endShrink: Float) {
            expand = startExpand..startShrink
            shrink = startShrink..endShrink
        }

        fun setMove(startStand: Float, startMove: Float, endMove: Float) {
            stand = startStand..startMove
            move = startMove..endMove
        }

        fun setDeform(start: Float, startStretch: Float, endStretch: Float) {
            round = start..startStretch
            stretch = startStretch..endStretch
        }
    }

    var animationTimeRange: AnimationTimeRange = AnimationTimeRange()

    var bounceSize: Float = 1.0f

    var arcRadius: Float = 5.0f
    var controlPointOffsetY: Float = 4.0f
    var stretchLength: Float = 4.0f
    var elemMaxInterval: Float = 14.0f

    fun draw(width: Int, height: Int, lineWidth: Float, foregroundColor: Int, time: Float, canvas: Canvas?) {
        val arcCenterX = width / 2.0f
        var arcCenterY = height / 2.0f - elemMaxInterval - (arcRadius + lineWidth) / 2.0f
        var alpha: Float = 1f

        // move
        if (!animationTimeRange.stand.contains(time)) {
            val elapsed = time - animationTimeRange.move.start
            val distance: Float = 2f * (height / 2.0f - arcCenterY)
            arcCenterY += distance * min(1f, elapsed / animationTimeRange.move.length())
        }

        var radius = arcRadius

        if (animationTimeRange.expand.contains(time)) {
            val elapsed = time - animationTimeRange.expand.start
            radius = (arcRadius + bounceSize) * elapsed / animationTimeRange.expand.length()
        } else if (animationTimeRange.shrink.contains(time)) {
            val elapsed = time - animationTimeRange.shrink.start
            radius = arcRadius + bounceSize * (1 - elapsed / animationTimeRange.shrink.length())
        }

        if (animationTimeRange.round.contains(time)) {
            val paint: Paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = foregroundColor
            canvas?.drawCircle(arcCenterX, arcCenterY, radius, paint)
        } else {

            if (time >= animationTimeRange.fadeIn.start) {
                val elapsed = time - animationTimeRange.fadeIn.start
                alpha = 1 - elapsed / animationTimeRange.fadeIn.length()
            }

            val elapsed = time - animationTimeRange.stretch.start
            val path = path(min(1f, elapsed / animationTimeRange.stretch.length()), arcCenterX, arcCenterY)
            val paint: Paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = foregroundColor
            paint.alpha = max((alpha * 255).toInt(), 0)
            canvas?.drawPath(path, paint)
        }
    }

    private fun path(progress: Float, arcCenterX: Float, arcCenterY: Float): Path {
        val waterDropTop = Pair(arcCenterX, arcCenterY - arcRadius - stretchLength * progress)
        val path = Path()
        path.moveTo(waterDropTop)
        path.quadTo(arcCenterX + arcRadius, arcCenterY - controlPointOffsetY, arcCenterX + arcRadius, arcCenterY)
        val arcLeft = arcCenterX - arcRadius
        val arcTop = arcCenterY - arcRadius
        path.addArc(RectF(arcLeft, arcTop, arcLeft + 2 * arcRadius, arcTop + 2 * arcRadius), 0f, 180f)
        path.quadTo(arcCenterX - arcRadius, arcCenterY - controlPointOffsetY, waterDropTop)

        return path
    }
}