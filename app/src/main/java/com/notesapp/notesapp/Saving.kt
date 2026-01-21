package com.notesapp.notesapp

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedOutputStream
import kotlin.math.sqrt

@Composable
fun SaveSVGButton() {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenHeightDp
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/svg+xml")
    ) { uri ->
        uri?.let {
            saveSVGDataToUri(context, it, strokes, height, width)
        }
    }

// To trigger it (e.g., from a button):
    Button(onClick = { filePickerLauncher.launch("my_drawing.svg") }) {
        Text("Save Drawing")
    }
}

fun saveSVGDataToUri(context: Context, uri: Uri, paths: List<List<PenPoint>>, height: Int, width: Int) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { raw ->
            BufferedOutputStream(raw, 8192).use { outputStream ->
                Log.d("LOGGING", "hello")
                outputStream.write("<svg height=\"1000\" width=\"1000\" xmlns=\"http://www.w3.org/2000/svg\"> ".toByteArray())
                Log.d("LOGGING", "${paths.size}")
                for (i in 0 until paths.size) {
                    Log.d("LOGGING", "h")
                    var leftPoints = ArrayList<Offset>(paths[i].size)
                    var rightPoints = ArrayList<Offset>(paths[i].size)
                    for (k in 0 until paths[i].size) {
                        val normal = calculateNormal(paths[i], k)
                        val thickness = paths[i][k].thickness/2
                        leftPoints.add(paths[i][k].position + (normal * (thickness)))
                        rightPoints.add(paths[i][k].position - (normal * (thickness)))
                    }
                    Log.d("LOGGING", "h2")
                    val d = StringBuilder()
                    d.append("M ${leftPoints[0].x} ${leftPoints[0].y} ") // Start

                    leftPoints.forEach { d.append("L ").append(it.x).append(' ').append(it.y).append(' ') }
                    for (j in rightPoints.size - 1 downTo 0) {
                        val p = rightPoints[j]
                        d.append("L ").append(p.x).append(' ').append(p.y).append(' ')
                    }
                    outputStream.write("<path stroke=\"black\" d=\"$d Z\"/> ".toByteArray())
                    leftPoints.clear()
                    rightPoints.clear()
                    Log.d("LOGGING", "h1")
                }
                Log.d("LOGGING", "done")
                outputStream.write("</svg>".toByteArray())
                outputStream.flush()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
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