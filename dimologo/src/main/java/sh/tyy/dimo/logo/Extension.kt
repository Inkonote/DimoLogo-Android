package sh.tyy.dimo.logo

import android.graphics.Path

internal fun Path.moveTo(point: Pair<Float, Float>) {
    moveTo(point.first, point.second)
}

internal fun Path.lineTo(point: Pair<Float, Float>) {
    lineTo(point.first, point.second)
}

internal fun Path.quadTo(controlPointX: Float, controlPointY: Float, endPoint: Pair<Float, Float>) {
    quadTo(controlPointX, controlPointY, endPoint.first, endPoint.second)
}

internal fun ClosedFloatingPointRange<Float>.length(): Float {
    return endInclusive - start
}