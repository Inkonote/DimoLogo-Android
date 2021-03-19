package com.inkonote.dimo.logo

import android.graphics.*
import kotlin.math.abs

internal class Line {

    var width: Float = 3.0f
    var marginHorizontal: Float = 5.0f
    var stretchLengths: List<Float> = listOf(12.0f, -8.0f, 6.0f)
    var stretchTimeRanges: List<ClosedFloatingPointRange<Float>> = emptyList()

    fun draw(width: Int, height: Int, time: Float, canvas: Canvas?, foregroundColor: Int) {
        val centerPoint = PointF(width / 2f, height / 2f)
        val lineLength = width - 2 * marginHorizontal
        val startPoint = PointF(centerPoint.x - lineLength / 2f, centerPoint.y)
        val endPoint = PointF(centerPoint.x + lineLength / 2f, centerPoint.y)
        val maxOffsets: List<Float> = stretchLengths
        val midLineCenterPoint = PointF(centerPoint.x, centerPoint.y)

        for (i in 0 until 3) {

            val range = stretchTimeRanges[i]
            val maxOffset = maxOffsets[i]
            if (range.contains(time)) {
                val elapsed = time - range.start
                var progress = elapsed / range.length()
                if (progress > .5) {
                    progress = abs(1 - progress)
                }
                midLineCenterPoint.y += maxOffset * progress
                break
            }
        }

        val linePath = Path()
        val lineWith2: Float = this.width / 2f

        val leftTop = Pair(startPoint.x, startPoint.y - lineWith2)
        val centerTop = Pair(midLineCenterPoint.x, midLineCenterPoint.y - lineWith2)
        val rightTop = Pair(endPoint.x, endPoint.y - lineWith2)

        val leftBottom = Pair(startPoint.x, startPoint.y + lineWith2)
        val centerBottom = Pair(midLineCenterPoint.x, midLineCenterPoint.y + lineWith2)
        val rightBottom = Pair(endPoint.x, endPoint.y + lineWith2)

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
}