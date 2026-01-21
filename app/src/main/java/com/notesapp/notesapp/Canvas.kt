package com.notesapp.notesapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingScreen() {
    // This 'list' stores all the points our finger touches
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val primaryButtonPressed = currentEvent.buttons.isPrimaryPressed // main button on spen
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
                                val currentPosition = change.position
                                if (mode == 1) {
                                    val currentPressure = change.pressure
                                    val history = change.historical
                                    var prev:PenPoint? = null
                                    if (!points.isEmpty()) {
                                        prev = points[0]
                                    }
                                    history.forEach { historicalChange ->
                                        if (prev != null && (historicalChange.position - prev.position).getDistance() >= samplingThreshold) {
                                            points.add(PenPoint(historicalChange.position, pressureToThickness(currentPressure), currentPressure))
                                            prev = PenPoint(historicalChange.position, pressureToThickness(currentPressure), currentPressure)
                                        } else if (prev == null) {
                                            points.add(PenPoint(historicalChange.position, pressureToThickness(currentPressure), currentPressure))
                                            prev = PenPoint(historicalChange.position, pressureToThickness(currentPressure), currentPressure)
                                        }
                                    }
                                    points.add(PenPoint(currentPosition, pressureToThickness(currentPressure), currentPressure))
                                } else if (mode == 2) {
                                    val toBeErased = strokes.find { it.any {p -> (p.position - currentPosition).getDistance() < eraserThreshold} }
                                    strokes.remove(toBeErased)
                                }
                                change.consume()
                            }
                        }
                    }
                }
            }
    ) {
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