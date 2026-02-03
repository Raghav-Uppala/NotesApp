package com.notesapp.notesapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class PenPoint(
    val position: Offset,
    val thickness: Float,
)

data class StrokeComp(
    val leftEdges: List<Offset>,
    val rightEdges: List<Offset>,
    val strokePath: Path,
)

sealed class Element {
    data class Stroke(
        val rawPoints: MutableList<PenPoint> = mutableStateListOf(),
        var computed: StrokeComp? = null,
        var color: Color = primaryColor,
    ) : Element() {
        val isDot: Boolean get() = rawPoints.size == 1
        val isPath: Boolean get() = rawPoints.size > 1
    }
}