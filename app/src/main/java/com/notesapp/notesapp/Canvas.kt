package com.notesapp.notesapp

import android.graphics.DashPathEffect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas(
    resetTrigger: Int,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dragVersion by remember { mutableStateOf(0) }

    LaunchedEffect(resetTrigger) {
        scale = 1f
        offset = Offset.Zero
    }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val oldScale = scale
                    scale *= zoom
                    val scaleChange = scale / oldScale

                    offset += (center - centroid) * (1 - 1 / scaleChange)
                    offset += pan
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val primaryButtonPressed =
                        currentEvent.buttons.isPrimaryPressed // main button on spen
                    var mode = 1 // pen mode
                    if (primaryButtonPressed) {
                        mode = 2 // erase mode
                    } else if (lasso) {
                        mode = 3 // lasso mode
                    } else if (lassoMove) {
                        mode = 4
                    }
                    Log.d("noteapp", "${mode}")
                    val type = down.type
                    val id = down.id
                    val points = mutableStateListOf<PenPoint>()
                    strokes.add(Element.Stroke(rawPoints = points))
                    if (type == PointerType.Stylus) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.find { it.id == id }

                            if (change == null || !change.pressed) {
                                if (points.isEmpty()) {
                                    strokes.removeAt(strokes.lastIndex)
                                }
                                break;
                            } else {
                                val currentPosition = change.position
                                if (mode == 1) {
                                    val currentPressure = change.pressure
                                    if (points.isNotEmpty()) {
                                        if ((points.last().position - currentPosition).getDistance() > 2) {
                                            points.add(
                                                PenPoint(
                                                    currentPosition,
                                                    pressureToThickness(currentPressure)
                                                )
                                            )
                                        }
                                    } else {
                                        strokes[strokes.lastIndex]
                                        points.add(
                                            PenPoint(
                                                currentPosition,
                                                pressureToThickness(currentPressure)
                                            )
                                        )
                                    }
                                }
                                else if (mode == 2) {
                                    val eraserRadiusSq = eraserThreshold * eraserThreshold
                                    val toBeErased = strokes.find { element ->
                                        if (element !is Element.Stroke) return@find false

                                        // 1. Check raw points (current session drawing)
                                        val hitRaw = element.rawPoints.any { p ->
                                            val dx = p.position.x - currentPosition.x
                                            val dy = p.position.y - currentPosition.y
                                            (dx * dx + dy * dy) < eraserRadiusSq
                                        }
                                        if (hitRaw) return@find true

                                        // 2. Check computed edges (loaded from SVG)
                                        element.computed?.let { comp ->
                                            // Check left edges
                                            val hitLeft = comp.leftEdges.any { edge ->
                                                val dx = edge.x - currentPosition.x
                                                val dy = edge.y - currentPosition.y
                                                (dx * dx + dy * dy) < eraserRadiusSq
                                            }
                                            if (hitLeft) return@find true

                                            // Check right edges
                                            val hitRight = comp.rightEdges.any { edge ->
                                                val dx = edge.x - currentPosition.x
                                                val dy = edge.y - currentPosition.y
                                                (dx * dx + dy * dy) < eraserRadiusSq
                                            }
                                            return@find hitRight
                                        }

                                        false
                                    }

                                    toBeErased?.let { strokes.remove(it) }
                                }
                                else if (mode == 3) {
                                    if (lassoPoints.isNotEmpty() && (lassoPoints.last() - currentPosition).getDistance() < 2f) {
                                        continue
                                    }
                                    lassoPoints.add(currentPosition)
                                }
                                else if (mode == 4) {
                                    val delta = change.position - change.previousPosition
                                    lassoElems.forEach { index ->
                                        val stroke = strokes.getOrNull(index)
                                        if (stroke is Element.Stroke) {
                                            stroke.move(delta)
                                        }
                                    }
                                    dragVersion++
                                }
                                change.consume()
                            }
                        }
                    }

                    if (mode == 1) {
                        val lastElem = strokes.lastOrNull()
                        if (lastElem != null && lastElem is Element.Stroke && lastElem.isPath && lastElem.computed == null) {
                            Log.d("noteapp", "is it here?")
                            lastElem.computed = computePath(lastElem.rawPoints)
                        }
                    }
                    else if (mode == 3) {
                        lassoElems = selectLassoStrokes()
                        lassoPoints.clear()
                        lasso = false
                        lassoMove = true
                    }
                    else if (mode == 4) {
                        lassoMove = false
                        for (stroke in lassoElems) {
                            strokes[stroke].selected = false
                        }
                        lassoElems = emptyList()
                    }
                }
            }
    ) {

        val _version = dragVersion
        Log.d("noteapp", "${strokes.size}")
        for (stroke in strokes) {
            when (stroke) {
                is Element.Stroke ->  when {
                    stroke.isDot -> dotDraw(stroke)
                    stroke.isPath -> pathDraw(stroke)
                }
            }
        }
        if (lassoPoints.size > 1) {
            val path = Path().apply {
                moveTo(lassoPoints[0].x, lassoPoints[0].y)
                for (i in 1 until lassoPoints.size) {
                    lineTo(lassoPoints[i].x, lassoPoints[i].y)
                }
            }

            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 10f),
                        phase = 0f
                    )
                )
            )
        }

    }
}

fun DrawScope.dotDraw(element: Element.Stroke) {
    Log.d("noteapp", "${element.rawPoints}")
    drawCircle(
        color = element.color,
        radius = element.rawPoints[0].thickness / 2f,
        center = element.rawPoints[0].position
    )
}

fun DrawScope.pathDraw(element: Element.Stroke) {
    val comp = element.computed
    if (comp != null) {
        if (element.selected == true ) {
            drawPath(path = comp.strokePath, color = Color.Blue)
        } else {
            drawPath(path = comp.strokePath, color = element.color)
        }
    } else {
        Log.d("noteapp", "or here?")
        val computedPath = computePath(element.rawPoints).strokePath
        drawPath(path = computedPath, color = element.color)
    }
}

fun computePath(stroke: MutableList<PenPoint>): StrokeComp {
    val strokePath = Path()

    val leftEdges = mutableListOf<Offset>()
    val rightEdges = mutableListOf<Offset>()
    var lastValidAngle = 0f

// 1. Calculate all edge points first
    for (k in 0 until stroke.size) {
        val p = if (k > 0 && k < stroke.size - 1) {
            (stroke[k-1].position + stroke[k].position + stroke[k+1].position) / 3f
        } else {
            stroke[k].position
        }

        val r = stroke[k].thickness / 2f

        val next = if (k < stroke.size - 1) stroke[k + 1].position else p
        val prev = if (k > 0) stroke[k - 1].position else p
        val dist = (next - prev).getDistance()
        val angle = if (dist > 0.1f) {
            atan2((next.y - prev.y).toDouble(), (next.x - prev.x).toDouble()).toFloat()
        } else {
            lastValidAngle // Keep the previous angle if we aren't moving
        }
        lastValidAngle = angle

        val sinAngle = sin(angle.toDouble()).toFloat()
        val cosAngle = cos(angle.toDouble()).toFloat()

        leftEdges.add(Offset(p.x + r * sinAngle, p.y - r * cosAngle))
        rightEdges.add(Offset(p.x - r * sinAngle, p.y + r * cosAngle))
    }

// 2. Build the Path: Go up the left side, then down the right side
    strokePath.moveTo(leftEdges[0].x, leftEdges[0].y)

// Left side curve
    for (i in 1 until leftEdges.size) {
        val mid = (leftEdges[i-1] + leftEdges[i]) / 2f
        strokePath.quadraticTo(leftEdges[i-1].x, leftEdges[i-1].y, mid.x, mid.y)
    }
    strokePath.lineTo(leftEdges.last().x, leftEdges.last().y)

// Rounded tip at the end
    strokePath.lineTo(rightEdges.last().x, rightEdges.last().y)

// Right side curve (going backwards)
    for (i in rightEdges.size - 2 downTo 0) {
        val mid = (rightEdges[i+1] + rightEdges[i]) / 2f
        strokePath.quadraticTo(rightEdges[i+1].x, rightEdges[i+1].y, mid.x, mid.y)
    }

    strokePath.close()
    return StrokeComp(leftEdges = leftEdges, rightEdges = rightEdges, strokePath = strokePath)
}

fun selectLassoStrokes(): List<Int> {
    if (lassoPoints.size < 3) return emptyList()

    val lassoBounds = Rect(
        left = lassoPoints.minOf { it.x },
        top = lassoPoints.minOf { it.y },
        right = lassoPoints.maxOf { it.x },
        bottom = lassoPoints.maxOf { it.y }
    )

    val selectedIndices = mutableListOf<Int>()

    strokes.forEachIndexed { index, element ->
        if (element is Element.Stroke) {
            // Broad Phase
            if (lassoBounds.overlaps(element.bounds)) {
                // Narrow Phase
                val pointsToTest = if (element.rawPoints.isNotEmpty()) {
                    element.rawPoints.map { it.position }
                } else {
                    element.computed?.leftEdges ?: emptyList()
                }

                if (pointsToTest.any { isPointInLasso(it, lassoPoints) }) {
                    selectedIndices.add(index)
                    strokes[index].selected = true
                }
            }
        }
    }

    return selectedIndices
}

fun isPointInLasso(point: Offset, lasso: List<Offset>): Boolean {
    if (lasso.size < 3) return false
    var isInside = false
    var j = lasso.size - 1
    for (i in lasso.indices) {
        if ((lasso[i].y > point.y) != (lasso[j].y > point.y) &&
            (point.x < (lasso[j].x - lasso[i].x) * (point.y - lasso[i].y) /
                    (lasso[j].y - lasso[i].y) + lasso[i].x)) {
            isInside = !isInside
        }
        j = i
    }
    return isInside
}