package com.notesapp.notesapp.saving

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.notesapp.notesapp.PenPoint
import com.notesapp.notesapp.strokes
import kotlin.math.sqrt

@Composable
fun SaveButton() {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenHeightDp
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/svg+xml")
    ) { uri ->
        uri?.let {
            saveDataToUri(context, it, strokes, height, width)
        }
    }

// To trigger it (e.g., from a button):
    Button(onClick = { filePickerLauncher.launch("my_drawing.svg") }) {
        Text("Save Drawing")
    }
}

@Composable
fun LoadButton() {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenHeightDp
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val data = readFileFromUri(context, it)
            val splitData = ArrayList(data.split("<polyline|<circle".toRegex()))

            splitData.forEach {
                Log.d("LOGGING", "parsed data $it")
            }
        }
    }
    // To trigger it (e.g., from a button):
    Box {
        Button(
            onClick = {
                filePickerLauncher.launch(arrayOf("text/plain", "image/svg+xml"))
            },
            modifier = Modifier.offset (
                x = 200.dp
            )
        ) {
            Text("Load Drawing")
        }
    }
}

fun saveDataToUri(context: Context, uri: Uri, paths: List<List<PenPoint>>, height: Int, width: Int) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            var data = "<svg height=\"1000\" width=\"1000\" xmlns=\"http://www.w3.org/2000/svg\"> "
            val leftPoints = mutableListOf<Offset>()
            val rightPoints = mutableListOf<Offset>()
            for (i in 0 until paths.size) {
                for (k in 0 until paths[i].size) {
                    val normal = calculateNormal(paths[i], k)
                    val pressure = paths[i][k].pressure
                    leftPoints.add(paths[i][k].position + (normal * (pressure * 1.2f)))
                    rightPoints.add(paths[i][k].position - (normal * (pressure * 1.2f)))
                }
                val d = StringBuilder()
                d.append("M ${leftPoints[0].x} ${leftPoints[0].y} ") // Start

                leftPoints.forEach { d.append("L ${it.x} ${it.y} ") } // Walk down left
                rightPoints.reversed().forEach { d.append("L ${it.x} ${it.y} ") } // Walk back up right
                data += "<path stroke=\"black\" d=\"$d Z\"/> "
                leftPoints.clear()
                rightPoints.clear()
            }
            data += "</svg>"
            Log.d("LOGGING", "$data")
            outputStream.write(data.toByteArray())
            outputStream.flush()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun readFileFromUri(context: Context, uri: Uri): String {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.bufferedReader().use { it.readText() }
    } ?: ""
}

fun calculateNormal(points: List<PenPoint>, i: Int): Offset {
    var p1 = points[i].position
    var p2: Offset
    if (i == 0) {
        p2 = points[i+1].position
    } else {
        p2 = points[i-1].position
    }
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val length = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

    return if (length != 0f) {
        Offset(-dy / length, dx / length)
    } else {
        Offset(0f, 0f)
    }
}