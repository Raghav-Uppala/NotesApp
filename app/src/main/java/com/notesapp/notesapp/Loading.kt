package com.notesapp.notesapp

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Xml
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.xmlpull.v1.XmlPullParser

@Composable
fun LoadSVGButton() {
    //val height = LocalConfiguration.current.screenHeightDp
    //val width = LocalConfiguration.current.screenHeightDp
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            strokes = parseSvgPaths(context, it)
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

//fun readSVGFileFromUri(context: Context, uri: Uri): String {
//    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
//        inputStream.bufferedReader().use { it.readText() }
//    } ?: ""
//}

fun parseSvgPaths(context: Context, uri: Uri): SnapshotStateList<SnapshotStateList<PenPoint>> {
    val statePaths: SnapshotStateList<SnapshotStateList<PenPoint>> = mutableStateListOf()
    val parser: XmlPullParser = Xml.newPullParser()

    context.contentResolver.openInputStream(uri)?.use { input ->
        parser.setInput(input, null)
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name.equals("path", true)) {
                val d = parser.getAttributeValue(null, "d")
                if (!d.isNullOrEmpty()) {
                    val points = parsePathPoints(d)

                    val mid = points.size / 2
                    val leftPoints = points.take(mid)
                    val rightPoints = points.takeLast(mid).reversed()

                    val penPoints = SnapshotStateList<PenPoint>()
                    for (i in leftPoints.indices) {
                        val left = leftPoints[i]
                        val right = rightPoints[i]
                        val center = Offset((left.x + right.x) / 2f, (left.y + right.y) / 2f)
                        val thickness = (left - right).getDistance()
                        penPoints.add(PenPoint(center, thickness))
                    }
                    statePaths.add(penPoints)
                }
            }
            eventType = parser.next()
        }
    }

    return statePaths
}

fun parsePathPoints(d: String): List<Offset> {
    val points = mutableListOf<Offset>()
    val tokens = d.trim().split("\\s+".toRegex())
    var i = 0
    while (i < tokens.size) {
        when (tokens[i]) {
            "M", "L" -> {
                if (i + 2 < tokens.size) {
                    val x = tokens[i + 1].toFloatOrNull()
                    val y = tokens[i + 2].toFloatOrNull()
                    if (x != null && y != null) {
                        points.add(Offset(x, y))
                    }
                    i += 3
                } else break
            }
            "Z" -> i += 1
            else -> i += 1
        }
    }
    return points
}
