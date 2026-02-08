package com.notesapp.notesapp

import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath

data class PenPoint(
    var position: Offset,
    val thickness: Float,
)

data class StrokeComp(
    var leftEdges: List<Offset>,
    var rightEdges: List<Offset>,
    var strokePath: Path,
)

sealed class Element {
    var selected = false
    data class Stroke(
        val rawPoints: MutableList<PenPoint> = mutableStateListOf(),
        var computed: StrokeComp? = null,
        var color: Color = primaryColor,
    ) : Element() {
        val isDot: Boolean get() = rawPoints.size == 1
        val isPath: Boolean get() = rawPoints.size > 1 || computed?.leftEdges?.isNotEmpty() == true
        val bounds: Rect get() {
            if (rawPoints.isEmpty() && (computed == null || computed!!.leftEdges.isEmpty())) return Rect.Zero
            var minX: Float
            var minY: Float
            var maxX: Float
            var maxY: Float

            if(computed != null) {
                minX = computed!!.leftEdges[0].x
                minY = computed!!.leftEdges[0].y
                maxX = computed!!.leftEdges[0].x
                maxY = computed!!.leftEdges[0].y
                for (i in 1 until computed!!.leftEdges.size) {
                    val p = computed!!.leftEdges[i]
                    if (p.x < minX) minX = p.x
                    if (p.y < minY) minY = p.y
                    if (p.x > maxX) maxX = p.x
                    if (p.y > maxY) maxY = p.y
                }
                for (i in 1 until computed!!.rightEdges.size) {
                    val p = computed!!.rightEdges[i]
                    if (p.x < minX) minX = p.x
                    if (p.y < minY) minY = p.y
                    if (p.x > maxX) maxX = p.x
                    if (p.y > maxY) maxY = p.y
                }

                return Rect(minX, minY, maxX, maxY)
            } else {
                minX = rawPoints[0].position.x
                minY = rawPoints[0].position.y
                maxX = rawPoints[0].position.x
                maxY = rawPoints[0].position.y
                for (i in 1 until rawPoints.size) {
                    val p = rawPoints[i]
                    if (p.position.x < minX) minX = p.position.x
                    if (p.position.y < minY) minY = p.position.y
                    if (p.position.x > maxX) maxX = p.position.x
                    if (p.position.y > maxY) maxY = p.position.y
                }

                return Rect(minX, minY, maxX, maxY)
            }
        }
        fun move(delta: Offset) {
            // 1. Move raw points
            if (rawPoints.isNotEmpty()) {
                rawPoints.forEach { pt ->
                    pt.position = pt.position + delta
                }
            }

            computed?.let {
                it.leftEdges = it.leftEdges.map { pt -> pt + delta }
                it.rightEdges = it.rightEdges.map { pt -> pt + delta }
                val matrix = android.graphics.Matrix()
                matrix.postTranslate(delta.x, delta.y)
                it.strokePath.asAndroidPath().transform(matrix)
            }
        }
    }
}