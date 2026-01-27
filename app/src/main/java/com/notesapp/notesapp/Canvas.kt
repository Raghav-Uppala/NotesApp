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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingScreen(
    resetTrigger: Int,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var Centroid by remember { mutableStateOf<Offset>(Offset.Zero) }
    LaunchedEffect(resetTrigger) {
        scale = 1f
        offset = Offset.Zero
        Log.d("LOGGING", "t")
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

                    Centroid = (centroid - offset) / scale

                    Log.d("LOGGING", "$scale $zoom")
                    Log.d(
                        "LOGGING",
                        "${offset}"
                    )
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
                    strokes.add(points)
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
                                Log.d("LOGGING", "${change.position}")
                                val currentPosition = change.position
                                if (mode == 1) {
                                    val currentPressure = change.pressure
                                    val history = change.historical
                                    var prev: PenPoint? = null
                                    if (!points.isEmpty()) {
                                        prev = points[0]
                                    }
                                    history.forEach { historicalChange ->
                                        if (prev != null && (historicalChange.position - prev.position).getDistance() >= samplingThreshold) {
                                            points.add(
                                                PenPoint(
                                                    historicalChange.position,
                                                    pressureToThickness(currentPressure)
                                                )
                                            )
                                            prev = PenPoint(
                                                historicalChange.position,
                                                pressureToThickness(currentPressure)
                                            )
                                        } else if (prev == null) {
                                            points.add(
                                                PenPoint(
                                                    historicalChange.position,
                                                    pressureToThickness(currentPressure)
                                                )
                                            )
                                            prev = PenPoint(
                                                historicalChange.position,
                                                pressureToThickness(currentPressure)
                                            )
                                        }
                                    }
                                    points.add(
                                        PenPoint(
                                            currentPosition,
                                            pressureToThickness(currentPressure)
                                        )
                                    )
                                } else if (mode == 2) {
                                    val toBeErased =
                                        strokes.find { it.any { p -> (p.position - currentPosition).getDistance() < eraserThreshold } }
                                    strokes.remove(toBeErased)
                                }
                                change.consume()
                            }
                        }
                    }
                }
            }
    ) {
//        drawCircle(
//            color = Color.Blue,
//            radius = 10f, // Adjust size of the dot
//            center = Offset(x =  scale * (size.width/3), y =  scale * (size.height/3))
//        )
//        for (i in 0 until strokes.size) {
//            for (k in 0 until strokes[i].size) {
//                val normal = calculateNormal(strokes[i], k)
//                val pressure = strokes[i][k].thickness/2
//                drawCircle(
//                    color = Color.Red,
//                    radius = 1f, // Adjust size of the dot
//                    center = strokes[i][k].position + (normal * (pressure))
//                )
//                drawCircle(
//                    color = Color.Red,
//                    radius = 1f, // Adjust size of the dot
//                    center = strokes[i][k].position - (normal * (pressure))
//                )
//
//            }
//        }
        for (i in 0 until strokes.size) {
            if (strokes[i].size == 1) {
                drawCircle(
                    color = primaryColor,
                    radius = fontSizeDot, // Adjust size of the dot
                    center = strokes[i][0].position
                )
            } else {
                for (k in 0 until strokes[i].size -1 ) {
                    val start = strokes[i][k]
                    val end = strokes[i][k + 1]
                    drawLine (
                        color = primaryColor,
                        start = start.position,
                        end = end.position,
                        strokeWidth = start.thickness,
                        cap = StrokeCap.Round
                    )
                    //}
                }
            }
        }
    }
}