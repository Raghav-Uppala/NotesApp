package com.notesapp.notesapp

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingScreen(
    resetTrigger: Int,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                    val center = Offset(size.width /2f, size.height /2f)
                    val oldScale = scale
                    scale *= zoom
                    val scaleChange = scale / oldScale

                    offset += (center - centroid) * (1 -1 / scaleChange)
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
                    }
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
                                            points.add(PenPoint(currentPosition,pressureToThickness(currentPressure)))
                                        }
                                    } else {
                                        strokes[strokes.lastIndex]
                                        points.add(PenPoint(currentPosition,pressureToThickness(currentPressure)))
                                    }
                                } else if (mode == 2) {
                                    val toBeErased = strokes.find { element ->
                                        element is Element.Stroke &&
                                                element.rawPoints.any { p ->
                                                    val dx = p.position.x - currentPosition.x
                                                    val dy = p.position.y - currentPosition.y
                                                    // No square root! Just multiplication and addition.
                                                    (dx * dx + dy * dy) < eraserThreshold * eraserThreshold
                                                }
                                    }
                                    strokes.remove(toBeErased)
                                }
                                change.consume()
                            }
                        }
                    }
                    val lastElem = strokes.lastOrNull()
                    if (lastElem is Element.Stroke && lastElem.isPath) {
                        lastElem.computed = computePath(lastElem.rawPoints)
                    }
                }
            }
    ) {
        var index = 0
        for (stroke in strokes) {
            when (stroke) {
                is Element.Stroke ->  when {
                    stroke.isDot -> dotDraw(stroke)
                    stroke.isPath -> pathDraw(stroke)
                }
            }
//            if (stroke.size == 1) {
//                drawCircle(
//                    color = primaryColor,
//                    radius = stroke[0].thickness / 2f,
//                    center = stroke[0].position
//                )
//            } else if (stroke.size > 1) {
//                val strokePath = Path()
//                if (stroke.size < 2) continue
//
//                val leftEdges = mutableListOf<Offset>()
//                val rightEdges = mutableListOf<Offset>()
//                var lastValidAngle = 0f
//
//// 1. Calculate all edge points first
//                for (k in 0 until stroke.size) {
//                    val p = if (k > 0 && k < stroke.size - 1) {
//                        (stroke[k-1].position + stroke[k].position + stroke[k+1].position) / 3f
//                    } else {
//                        stroke[k].position
//                    }
//
//                    val r = stroke[k].thickness / 2f
//
//                    val next = if (k < stroke.size - 1) stroke[k + 1].position else p
//                    val prev = if (k > 0) stroke[k - 1].position else p
//                    val dist = (next - prev).getDistance()
//                    val angle = if (dist > 0.1f) {
//                        atan2((next.y - prev.y).toDouble(), (next.x - prev.x).toDouble()).toFloat()
//                    } else {
//                        lastValidAngle // Keep the previous angle if we aren't moving
//                    }
//                    lastValidAngle = angle
//
//                    val sinAngle = sin(angle.toDouble()).toFloat()
//                    val cosAngle = cos(angle.toDouble()).toFloat()
//
//                    leftEdges.add(Offset(p.x + r * sinAngle, p.y - r * cosAngle))
//                    rightEdges.add(Offset(p.x - r * sinAngle, p.y + r * cosAngle))
//                }
//
//// 2. Build the Path: Go up the left side, then down the right side
//                strokePath.moveTo(leftEdges[0].x, leftEdges[0].y)
//
//// Left side curve
//                for (i in 1 until leftEdges.size) {
//                    val mid = (leftEdges[i-1] + leftEdges[i]) / 2f
//                    strokePath.quadraticTo(leftEdges[i-1].x, leftEdges[i-1].y, mid.x, mid.y)
//                }
//
//// Rounded tip at the end
//                strokePath.lineTo(rightEdges.last().x, rightEdges.last().y)
//
//// Right side curve (going backwards)
//                for (i in rightEdges.size - 2 downTo 0) {
//                    val mid = (rightEdges[i+1] + rightEdges[i]) / 2f
//                    strokePath.quadraticTo(rightEdges[i+1].x, rightEdges[i+1].y, mid.x, mid.y)
//                }
//
//                strokePath.close()
//
//// 3. Draw as a single filled shape
//                drawPath(path = strokePath, color = primaryColor)
//            }
//            index++
        }
    }
}

fun DrawScope.dotDraw(element: Element.Stroke) {
    drawCircle(
        color = element.color,
        radius = element.rawPoints[0].thickness / 2f,
        center = element.rawPoints[0].position
    )
}

fun DrawScope.pathDraw(element: Element.Stroke) {
    val comp = element.computed
    if (comp != null) {
        drawPath(path = comp.strokePath, color = element.color)
        Log.d("noteapp", "used cache")
    } else {
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