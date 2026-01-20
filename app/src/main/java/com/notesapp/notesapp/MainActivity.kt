package com.notesapp.notesapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Surface { com.notesapp.notesapp.DrawingScreen() } }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingScreen() {
    // This 'list' stores all the points our finger touches
    val strokes = remember { mutableStateListOf<SnapshotStateList<Offset>>() }
    val primaryColor = Color.Blue
    val samplingThreshold = 4f
    val eraserThreshold = 10f
    Canvas(
        modifier = Modifier
            .fillMaxSize()
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
                    val points = mutableStateListOf<Offset>()
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
                                    //val currentPressure = change.pressure
                                    val history = change.historical
                                    var prev:Offset? = null
                                    if (!points.isEmpty()) {
                                        prev = points[0]
                                    }
                                    history.forEach { historicalChange ->
                                        if (prev != null && (historicalChange.position - prev).getDistance() >= samplingThreshold) {
                                            points.add(historicalChange.position)
                                            prev = historicalChange.position
                                        } else if (prev == null) {
                                            points.add(historicalChange.position)
                                            prev = historicalChange.position
                                        }
                                    }
                                    points.add(currentPosition)
                                } else if (mode == 2) {
                                    val toBeErased = strokes.find { it.any {p -> (p - currentPosition).getDistance() < eraserThreshold} }
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
                    radius = 5f, // Adjust size of the dot
                    center = strokes[i][0]
                )
            } else {
                for (k in 0 until strokes[i].size -1 ) {
                    var prev = strokes[i][0]
                    if (k != 0) {
                        prev = strokes[i][k-1]
                    }
                    val start = strokes[i][k]
                    val end = strokes[i][k + 1]
                    drawLine (
                        color = primaryColor,
                        start = start,
                        end = end,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                    //}
                }
            }
        }
    }
}